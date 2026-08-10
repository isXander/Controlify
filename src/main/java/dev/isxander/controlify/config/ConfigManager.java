/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.config;

import com.google.gson.*;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import dev.isxander.controlify.config.dto.SharedConfig;
import dev.isxander.controlify.config.dto.dfu.ControlifyDataFixer;
import dev.isxander.controlify.config.dto.profile.ProfileConfig;
import dev.isxander.controlify.config.settings.ControlifySettings;
import dev.isxander.controlify.config.settings.profile.ProfileSettings;
import dev.isxander.controlify.debug.DebugProperties;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;

public class ConfigManager implements AutoCloseable {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Pattern PROFILE_FILE = Pattern.compile("profile-(\\d+)\\.json");

	private final Path configDirectory;
	private final Path sharedPath;
	private final Path legacyPath;
	private @Nullable ConfigMigrator configMigrator;

	private ControlifySettings settings = ControlifySettings.defaults();
	private int activeProfileIndex = -1;
	private FileChannel activeLockChannel;
	private FileLock activeLock;
	private boolean dirty;

	public ConfigManager(Path minecraftConfigDirectory) {
		this.configDirectory = minecraftConfigDirectory.resolve("controlify");
		this.sharedPath = configDirectory.resolve("controlify.json");
		this.legacyPath = minecraftConfigDirectory.resolve("controlify.json");
	}

	public ControlifySettings getSettings() {
		return settings;
	}

	public int getActiveProfileIndex() {
		return activeProfileIndex;
	}

	public ProfileSettings getActiveProfile() {
		ProfileSettings profile = settings.getProfileSettings(activeProfileIndex);
		if (profile == null) {
			throw new IllegalStateException("No active Controlify profile");
		}
		return profile;
	}

	public void loadOrDefault() {
		try {
			Files.createDirectories(configDirectory);
			if (Files.exists(sharedPath)) {
				loadShared();
			} else if (Files.exists(legacyPath)) {
				migrateLegacy();
			} else {
				settings = ControlifySettings.defaults();
				writeShared();
			}

			loadProfiles();
			selectStartupProfile();
		} catch (IOException e) {
			throw new RuntimeException("Failed to initialise Controlify configuration", e);
		}
	}

	private void loadShared() throws IOException {
		var migrated = this.configMigrator().migrateShared(readJson(sharedPath));
		settings = ControlifySettings.fromSharedDTO(migrated.config());
		if (migrated.requiresSaving()) {
			writeShared();
		}
	}

	private void migrateLegacy() throws IOException {
		var migrated = this.configMigrator().migrateLegacy(readJson(legacyPath));
		settings = ControlifySettings.fromSharedDTO(migrated.shared().config());

		for (int index = 0; index < migrated.profiles().size(); index++) {
			settings.putProfileSettings(index, ProfileSettings.fromDTO(migrated.profiles().get(index).config()));
		}

		if (settings.profileSettings().isEmpty()) {
			settings.putProfileSettings(0, ProfileSettings.createDefault());
		}

		for (int index : settings.profileSettings().keySet()) {
			writeProfile(index, settings.getProfileSettings(index));
		}
		writeShared();
		archiveLegacy();
		LOGGER.info("Migrated legacy Controlify client config into {}", configDirectory.toAbsolutePath());
	}

	private void loadProfiles() throws IOException {
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(configDirectory, "profile-*.json")) {
			for (Path path : stream) {
				Matcher matcher = PROFILE_FILE.matcher(path.getFileName().toString());
				if (!matcher.matches()) {
					LOGGER.warn("Ignoring invalid Controlify profile filename {}", path.getFileName());
					continue;
				}
				int serializedIndex;
				try {
					serializedIndex = Integer.parseInt(matcher.group(1));
				} catch (NumberFormatException e) {
					LOGGER.warn("Ignoring out-of-range Controlify profile filename {}", path.getFileName());
					continue;
				}
				if (serializedIndex < 1) {
					LOGGER.warn("Ignoring invalid Controlify profile filename {}", path.getFileName());
					continue;
				}
				int index = serializedIndex - 1;
				try {
					settings.putProfileSettings(index, readProfile(path).settings());
				} catch (IOException e) {
					LOGGER.error("Failed to read Controlify profile {}", index, e);
				}
			}
		}
	}

	private ProfileLoad readProfile(Path path) throws IOException {
		var migrated = this.configMigrator().migrateProfile(readJson(path));
		return new ProfileLoad(ProfileSettings.fromDTO(migrated.config()), migrated.requiresSaving());
	}

	private void selectStartupProfile() throws IOException {
		if (DebugProperties.PROFILE != null) {
			if (!trySelectProfile(DebugProperties.PROFILE, true)) {
				throw new IllegalStateException("Controlify profile " + DebugProperties.PROFILE + " is locked by another client");
			}
			return;
		}

		int preferred = settings.globalSettings().preferredProfile;
		if (trySelectProfile(preferred, true)) {
			return;
		}

		for (int index : settings.profileSettings().keySet()) {
			if (index != preferred && trySelectProfile(index, false)) {
				return;
			}
		}

		int next = settings.profileSettings().isEmpty() ? 0 : settings.profileSettings().lastIntKey() + 1;
		if (!trySelectProfile(next, true)) {
			throw new IOException("Failed to lock newly-created Controlify profile " + next);
		}
	}

	private boolean trySelectProfile(int index, boolean createIfMissing) throws IOException {
		LockHandle handle = tryLock(index);
		if (handle == null) {
			return false;
		}

		ProfileSettings profile = settings.getProfileSettings(index);
		if (profile == null && !createIfMissing) {
			handle.close();
			return false;
		}
		if (profile == null) {
			if (Files.exists(profilePath(index))) {
				makeBackup(profilePath(index));
			}
			profile = ProfileSettings.createDefault();
			settings.putProfileSettings(index, profile);
			writeProfile(index, profile);
		} else {
			Path profilePath = profilePath(index);
			if (Files.exists(profilePath)) {
				try {
					ProfileLoad loaded = readProfile(profilePath);
					profile = loaded.settings();
					settings.putProfileSettings(index, profile);
					if (loaded.requiresSaving()) {
						writeProfile(index, profile);
					}
				} catch (IOException e) {
					makeBackup(profilePath);
					profile = ProfileSettings.createDefault();
					settings.putProfileSettings(index, profile);
					writeProfile(index, profile);
				}
			}
		}

		activeProfileIndex = index;
		activeLockChannel = handle.channel();
		activeLock = handle.lock();
		LOGGER.info("Selected and locked Controlify profile {}", index);
		return true;
	}

	public int createProfile() throws IOException {
		int index = settings.profileSettings().isEmpty()
				? 0
				: Math.addExact(settings.profileSettings().lastIntKey(), 1);
		while (true) {
			LockHandle handle = tryLock(index);
			if (handle != null) {
				try (handle) {
					if (!Files.exists(profilePath(index))) {
						ProfileSettings profile = ProfileSettings.createDefault();
						writeProfile(index, profile);
						settings.putProfileSettings(index, profile);
						LOGGER.info("Created Controlify profile {}", index);
						return index;
					}
				}
			}
			index = Math.addExact(index, 1);
		}
	}

	public boolean switchProfile(int index, boolean remember) throws IOException {
		if (index < 0 || settings.getProfileSettings(index) == null) {
			return false;
		}
		if (index == activeProfileIndex) {
			if (remember && settings.globalSettings().preferredProfile != index) {
				settings.globalSettings().preferredProfile = index;
				writeShared();
			}
			return true;
		}

		writeProfile(activeProfileIndex, getActiveProfile());

		LockHandle handle = tryLock(index);
		if (handle == null) {
			return false;
		}

		ProfileSettings profile = settings.getProfileSettings(index);
		try {
			Path profilePath = profilePath(index);
			if (Files.exists(profilePath)) {
				try {
					ProfileLoad loaded = readProfile(profilePath);
					profile = loaded.settings();
					if (loaded.requiresSaving()) {
						writeProfile(index, profile);
					}
				} catch (IOException e) {
					makeBackup(profilePath);
					profile = ProfileSettings.createDefault();
					writeProfile(index, profile);
				}
			}

			if (remember && settings.globalSettings().preferredProfile != index) {
				int previousPreferred = settings.globalSettings().preferredProfile;
				settings.globalSettings().preferredProfile = index;
				try {
					writeShared();
				} catch (IOException e) {
					settings.globalSettings().preferredProfile = previousPreferred;
					throw e;
				}
			}
		} catch (IOException | RuntimeException | Error t) {
			try {
				handle.close();
			} catch (IOException closeError) {
				t.addSuppressed(closeError);
			}
			throw t;
		}

		FileChannel previousChannel = activeLockChannel;
		FileLock previousLock = activeLock;
		settings.putProfileSettings(index, profile);
		activeProfileIndex = index;
		activeLockChannel = handle.channel();
		activeLock = handle.lock();
		closeLock(previousLock, previousChannel);
		LOGGER.info("Selected and locked Controlify profile {}", index);
		return true;
	}

	public boolean deleteProfile(int index) throws IOException {
		if (index < 0 || index == activeProfileIndex || settings.getProfileSettings(index) == null) {
			return false;
		}

		LockHandle handle = tryLock(index);
		if (handle == null) {
			return false;
		}

		try (handle) {
			if (settings.globalSettings().preferredProfile == index) {
				int previousPreferred = settings.globalSettings().preferredProfile;
				settings.globalSettings().preferredProfile = activeProfileIndex;
				try {
					writeShared();
				} catch (IOException e) {
					settings.globalSettings().preferredProfile = previousPreferred;
					throw e;
				}
			}
			Files.deleteIfExists(profilePath(index));
			settings.removeProfileSettings(index);
		}

		LOGGER.info("Deleted Controlify profile {}", index);
		return true;
	}

	private @Nullable LockHandle tryLock(int index) throws IOException {
		FileChannel channel = FileChannel.open(lockPath(index), CREATE, WRITE);
		try {
			FileLock lock = channel.tryLock();
			if (lock == null) {
				channel.close();
				return null;
			}
			return new LockHandle(channel, lock);
		} catch (OverlappingFileLockException e) {
			channel.close();
			return null;
		}
	}

	public boolean save() throws IOException {
		writeShared();
		if (activeProfileIndex >= 0) {
			writeProfile(activeProfileIndex, getActiveProfile());
		}
		dirty = false;
		return true;
	}

	public boolean saveSafely() {
		try {
			return save();
		} catch (IOException e) {
			LOGGER.error("Failed to save Controlify config", e);
			return false;
		}
	}

	public void markDirty() {
		dirty = true;
	}

	public void saveIfDirty() {
		if (dirty) {
			saveSafely();
		}
	}

	private void writeShared() throws IOException {
		writeCodec(sharedPath, SharedConfig.CODEC, settings.toSharedDTO());
	}

	private void writeProfile(int index, ProfileSettings profile) throws IOException {
		writeCodec(profilePath(index), ProfileConfig.CODEC, profile.toDTO());
	}

	private <T> void writeCodec(Path path, Codec<T> codec, T value) throws IOException {
		JsonObject root = new JsonObject();
		root.addProperty("schema_version", ControlifyDataFixer.CURRENT_VERSION);
		JsonObject encoded = codec.encodeStart(JsonOps.INSTANCE, value)
				.result()
				.orElseThrow(() -> new IOException("Failed to encode " + path))
				.getAsJsonObject();
		encoded.entrySet().forEach(entry -> root.add(entry.getKey(), entry.getValue()));

		String json = new GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(root);
		Path temporary = Files.createTempFile(configDirectory, path.getFileName().toString(), ".tmp");
		try {
			Files.writeString(temporary, json);
			try {
				Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
			} catch (AtomicMoveNotSupportedException e) {
				Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
			}
		} finally {
			Files.deleteIfExists(temporary);
		}
	}

	private ConfigMigrator configMigrator() {
		if (this.configMigrator == null) {
			this.configMigrator = new ConfigMigrator(
				ProfileSettings.createDefault().toDTO(),
				ControlifyDataFixer.getFixer()
			);
		}
		return this.configMigrator;
	}

	private static JsonObject readJson(Path path) throws IOException {
		try {
			return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
		} catch (JsonParseException | IllegalStateException e) {
			throw new IOException("Failed to parse " + path, e);
		}
	}

	private void makeBackup(Path path) throws IOException {
		int index = 0;
		Path backup;
		do {
			backup = path.resolveSibling(path.getFileName() + ".backup" + (index == 0 ? "" : index));
			index++;
		} while (Files.exists(backup));
		Files.copy(path, backup);
	}

	private void archiveLegacy() throws IOException {
		int index = 0;
		Path backup;
		do {
			backup = legacyPath.resolveSibling(legacyPath.getFileName() + ".backup" + (index == 0 ? "" : index));
			index++;
		} while (Files.exists(backup));
		Files.move(legacyPath, backup);
	}

	private Path profilePath(int index) {
		return configDirectory.resolve("profile-" + serializedProfileIndex(index) + ".json");
	}

	private Path lockPath(int index) {
		return configDirectory.resolve("profile-" + serializedProfileIndex(index) + ".lock");
	}

	private static int serializedProfileIndex(int index) {
		return Math.addExact(index, 1);
	}

	private static void closeLock(@Nullable FileLock lock, @Nullable FileChannel channel) {
		try {
			if (lock != null && lock.isValid()) {
				lock.release();
			}
		} catch (IOException e) {
			LOGGER.error("Failed to release Controlify profile lock", e);
		} finally {
			if (channel != null) {
				try {
					channel.close();
				} catch (IOException e) {
					LOGGER.error("Failed to close Controlify profile lock channel", e);
				}
			}
		}
	}

	@Override
	public void close() {
		saveIfDirty();
		closeLock(activeLock, activeLockChannel);
		activeLock = null;
		activeLockChannel = null;
	}

	private record LockHandle(FileChannel channel, FileLock lock) implements AutoCloseable {
		@Override
		public void close() throws IOException {
			lock.release();
			channel.close();
		}
	}

	private record ProfileLoad(ProfileSettings settings, boolean requiresSaving) {
	}
}
