<script setup lang="ts">
import defaultController from '../../../../src/main/resources/assets/controlify/textures/gui/sprites/controllers/default.png'
import defaultAltController from '../../../../src/main/resources/assets/controlify/textures/gui/sprites/controllers/default_alt.png'
import dualsenseController from '../../../../src/main/resources/assets/controlify/textures/gui/sprites/controllers/dualsense.png'
import dualshock3Controller from '../../../../src/main/resources/assets/controlify/textures/gui/sprites/controllers/dualshock3.png'
import dualshock4Controller from '../../../../src/main/resources/assets/controlify/textures/gui/sprites/controllers/dualshock4.png'
import pspController from '../../../../src/main/resources/assets/controlify/textures/gui/sprites/controllers/psp.png'
import steamController from '../../../../src/main/resources/assets/controlify/textures/gui/sprites/controllers/steam_controller.png'
import steamDeckController from '../../../../src/main/resources/assets/controlify/textures/gui/sprites/controllers/steam_deck.png'
import switchController from '../../../../src/main/resources/assets/controlify/textures/gui/sprites/controllers/switch.png'
import xboxController from '../../../../src/main/resources/assets/controlify/textures/gui/sprites/controllers/xbox_one.png'

const controllers = [
  { name: 'Xbox', image: xboxController },
  { name: 'DualSense', image: dualsenseController },
  { name: 'DualShock 4', image: dualshock4Controller },
  { name: 'Switch', image: switchController },
  { name: 'Steam Deck', image: steamDeckController },
  { name: 'Steam Controller', image: steamController },
  { name: 'PlayStation Portable', image: pspController },
  { name: 'DualShock 3', image: dualshock3Controller },
  { name: 'Generic gamepad', image: defaultController },
  { name: 'XInput gamepad', image: defaultAltController }
]

const reverseControllers = [...controllers].reverse()

const featureMarquees = [
  'Rumble · Gyro aiming · Adaptive triggers · Radial menu · Button guides · Full remapping · Virtual mouse · Data driven ·',
  'Mod compatibility · SDL3 input · On-screen keyboard · Flick stick · Multiplayer · Resource packs · Controller glyphs · Mixed input ·'
]
</script>

<template>
  <main class="controlify-home">
    <section class="home-hero">
      <div class="home-hero__grid" aria-hidden="true"></div>

      <div class="home-shell home-hero__layout">
        <div class="home-hero__copy">
          <p class="home-platform">Fabric + NeoForge</p>
          <h1>
            <span>Minecraft Java.</span>
            <strong>On a controller.</strong>
          </h1>
          <p class="home-hero__summary">
            Use a controller for gameplay, inventories, chat and modded screens.
            Get matching button glyphs, configurable binds, rumble, gyro and adaptive triggers.
          </p>

          <div class="home-actions">
            <a class="home-button home-button--acid" href="https://modrinth.com/mod/controlify">
              Download Controlify
              <span aria-hidden="true">↗</span>
            </a>
            <a class="home-button home-button--outline" href="/users/controller-support/">
              Supported controllers
              <span aria-hidden="true">→</span>
            </a>
          </div>

          <p class="home-hero__facts">SDL3 input <i>•</i> Fully remappable <i>•</i> Open source</p>
        </div>

        <div class="controller-display" role="img" aria-label="A connected pixel art DualSense controller">
          <img :src="steamController" alt="" class="controller-display__controller">
        </div>
      </div>
    </section>

    <section class="controller-section" aria-labelledby="controllers-heading">
      <div class="home-shell controller-section__header">
        <h2 id="controllers-heading">Any controller.</h2>
        <div>
          <p>
            Controlify uses SDL3 to support gamepads and joysticks across Windows,
            Linux and macOS. It identifies common controllers and loads the right
            glyphs and defaults automatically.
          </p>
          <a class="home-text-link" href="/users/controller-support/">Check controller support <span aria-hidden="true">→</span></a>
        </div>
      </div>

      <div class="controller-lanes" role="img" aria-label="Pixel art controllers supported by Controlify">
        <div class="controller-lane">
          <div class="controller-lane__track">
            <div v-for="pass in 2" :key="`forward-${pass}`" class="controller-lane__group" aria-hidden="true">
              <div v-for="controller in controllers" :key="`${pass}-${controller.name}-${controller.image}`" class="controller-chip">
                <img :src="controller.image" alt="">
                <span>{{ controller.name }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="controller-lane controller-lane--reverse">
          <div class="controller-lane__track">
            <div v-for="pass in 2" :key="`reverse-${pass}`" class="controller-lane__group" aria-hidden="true">
              <div v-for="controller in reverseControllers" :key="`${pass}-${controller.name}-${controller.image}`" class="controller-chip">
                <img :src="controller.image" alt="">
                <span>{{ controller.name }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="integration-section" aria-labelledby="integration-heading">
      <div class="home-shell">
        <header class="section-heading section-heading--split">
          <h2 id="integration-heading">Mod compatibility.</h2>
          <p>
            Controlify makes it easy for other mods to add enhanced support.
            Sodium settings, JourneyMap, Minimega and other modded experiences
            can be easily achieved!
          </p>
        </header>

        <div class="integration-gallery">
          <figure class="game-shot game-shot--sodium">
            <img src="/images/sodium-options.webp" alt="Sodium's settings screen being operated with a controller" width="2560" height="1372" loading="lazy">
            <figcaption><strong><a href="https://modrinth.com/mod/reeses-sodium-options">Reese's Sodium Options</a></strong><span>Tabs, sliders, toggles and prompts</span></figcaption>
          </figure>

          <figure class="game-shot game-shot--minimega-glide">
            <img src="/images/minimega-glide.webp" alt="Playing Minimega Glide with Controlify" width="2560" height="1372" loading="lazy">
            <figcaption><strong><a href="https://modrinth.com/mod/minimega">Minimega</a> Glide</strong><span>Custom actions and button guides</span></figcaption>
          </figure>

          <figure class="game-shot game-shot--journeymap-fullscreen">
            <img src="/images/journeymap-fullscreen.webp" alt="JourneyMap fullscreen interface with Controlify" width="2560" height="1372" loading="lazy">
            <figcaption><strong><a href="https://modrinth.com/plugin/journeymap">JourneyMap</a></strong><span>Controller-ready game screens</span></figcaption>
          </figure>
        </div>

        <div class="integration-docs">
          <a href="/developers/getting-started/" class="integration-doc">
            <span>For mod developers</span>
            <h3>Add controller support to a mod</h3>
            <p>Register bindings and make custom screens handle focus, lists, sliders and virtual mouse input.</p>
            <strong>Open the developer docs <i aria-hidden="true">→</i></strong>
          </a>

          <a href="/resource-packs/custom-controller-identification/" class="integration-doc">
            <span>For resource packs</span>
            <h3>Change how controllers look and behave</h3>
            <p>Provide controller identities, glyphs, default binds, guides, keyboard layouts and radial icons.</p>
            <strong>Open the resource pack docs <i aria-hidden="true">→</i></strong>
          </a>
        </div>
      </div>
    </section>

    <section class="features-section" aria-labelledby="features-heading">
      <div class="feature-type" aria-hidden="true">
        <div
          v-for="(features, lane) in featureMarquees"
          :key="features"
          class="feature-type__lane"
          :class="{ 'feature-type__lane--reverse': lane === 1 }"
        >
          <div class="feature-type__track">
            <div v-for="pass in 2" :key="pass" class="feature-type__group">{{ features }}</div>
          </div>
        </div>
      </div>

      <div class="home-shell">
        <header class="section-heading section-heading--features">
          <h2 id="features-heading">Features.</h2>
          <p>These are built into Controlify and can be configured per controller.</p>
        </header>

        <div class="hardware-features">
          <article class="hardware-card hardware-card--rumble">
            <div class="rumble-rings" aria-hidden="true"><span></span><span></span><span></span><span></span></div>
            <div class="rumble-controller" aria-hidden="true">
              <img :src="xboxController" alt="" class="rumble-controller__moving">
              <img :src="xboxController" alt="" class="rumble-controller__still">
            </div>
            <div class="hardware-card__copy">
              <h3>Rumble</h3>
              <p>Configurable vibration for damage, block breaking, lightning, item use and other game events.</p>
            </div>
          </article>

          <article class="hardware-card hardware-card--gyro">
            <div class="gyro-display" aria-hidden="true">
              <span></span>
              <img :src="dualsenseController" alt="">
            </div>
            <div class="hardware-card__copy">
              <h3>Gyro</h3>
              <p>Use motion for precise aim and flick stick for immediate turns on supported controllers.</p>
            </div>
          </article>

          <article class="hardware-card hardware-card--triggers">
            <img src="/images/hypixel-pit-bow.webp" alt="Drawing a bow in Hypixel Pit with Controlify" width="2560" height="1372" loading="lazy">
            <div class="trigger-meter" aria-hidden="true">
              <span>L2</span><i></i><i></i><i></i><i></i><i></i><i></i><i></i><i></i>
            </div>
            <div class="hardware-card__copy">
              <h3>Adaptive triggers</h3>
              <p>DualSense trigger resistance changes for bows, crossbows, weapons, food, shields and other items.</p>
              <a href="/resource-packs/adaptive-trigger-effects/">Trigger effect docs <span aria-hidden="true">→</span></a>
            </div>
          </article>
        </div>

        <div class="screen-features">
          <article class="screen-feature screen-feature--radial">
            <img src="/images/radial-menu.webp" alt="Controlify's radial menu open in Minecraft" width="2560" height="1372" loading="lazy">
            <div>
              <h3>Radial menu</h3>
              <p>Put occasional actions on a configurable wheel and keep frequently used controls on physical buttons.</p>
              <a href="/resource-packs/radial-icons/">Radial menu docs <span aria-hidden="true">→</span></a>
            </div>
          </article>

          <article class="screen-feature screen-feature--keyboard">
            <img src="/images/chat-with-keyboard.webp" alt="Typing in Minecraft chat with Controlify's on-screen keyboard" width="2560" height="1372" loading="lazy">
            <div>
              <h3>On-screen keyboard</h3>
              <p>Write chat messages, commands and signs without reaching for a physical keyboard.</p>
              <a href="/resource-packs/keyboard-layouts/">Keyboard layout docs <span aria-hidden="true">→</span></a>
            </div>
          </article>

          <article class="screen-feature screen-feature--guides">
            <img src="/images/container-guide.webp" alt="Controller shortcuts displayed beside a Minecraft inventory" width="2560" height="1369" loading="lazy">
            <div>
              <h3>Button guides</h3>
              <p>Gameplay and menus show the available actions with glyphs for the connected controller.</p>
              <a href="/resource-packs/guides/">Button guide docs <span aria-hidden="true">→</span></a>
            </div>
          </article>

          <article class="screen-feature screen-feature--binds">
            <img src="/images/controls-screen.webp" alt="Controlify's controller binding screen" width="2560" height="1372" loading="lazy">
            <div>
              <h3>Full remapping</h3>
              <p>Bind vanilla and modded actions, tune sensitivity and deadzones, and save settings per controller.</p>
            </div>
          </article>
        </div>
      </div>
    </section>

    <section class="multiplayer-section" aria-labelledby="multiplayer-heading">
      <div class="home-shell">
        <header class="section-heading section-heading--multiplayer">
          <h2 id="multiplayer-heading">Multiplayer.</h2>
          <div>
            <p>
              Controlify works on normal Minecraft servers. The server does not need
              Controlify installed.
            </p>
            <p>
              Stick movement defaults to keyboard-like WASD input on servers because
              some anti-cheat systems reject analogue movement. Servers can allow full
              analogue input, and trusted servers can be added to a local whitelist.
            </p>
          </div>
        </header>

        <div class="multiplayer-facts" role="list" aria-label="Multiplayer support details">
          <span role="listitem">No server mod required</span>
          <span role="listitem">Keyboard-like movement by default</span>
          <span role="listitem">Analogue server whitelist</span>
        </div>

        <div class="multiplayer-gallery">
          <figure class="game-shot game-shot--pit">
            <img src="/images/hypixel-pit-fighting.webp" alt="Fighting in Hypixel Pit with Controlify" width="2560" height="1372" loading="lazy">
            <figcaption><strong><a href="https://hypixel.net/the-pit/">Hypixel Pit</a></strong><span>Combat on a public server</span></figcaption>
          </figure>

          <figure class="game-shot game-shot--wall">
            <img src="/images/hole-in-the-wall.webp" alt="Playing Hole in the Wall on MCC Island with Controlify" width="2560" height="1372" loading="lazy">
            <figcaption><strong><a href="https://mccisland.net">MCC Island</a></strong><span>Hole in the Wall</span></figcaption>
          </figure>

          <figure class="game-shot game-shot--summit">
            <img src="/images/multiplayer-gameplay.webp" alt="Playing Smithed Summit with Controlify" width="2560" height="1372" loading="lazy">
            <figcaption><strong><a href="https://smithed.net/summit">Smithed Summit</a></strong><span>Datapacks!</span></figcaption>
          </figure>
        </div>
      </div>
    </section>

    <section class="download-section">
      <div class="home-shell download-section__inner">
        <img :src="xboxController" alt="" aria-hidden="true">
        <h2>Get Controlify.</h2>
        <p>Download the mod, connect a controller and open Minecraft.</p>
        <div class="home-actions home-actions--centered">
          <a class="home-button home-button--dark" href="https://modrinth.com/mod/controlify">
            Modrinth
            <span aria-hidden="true">↗</span>
          </a>
          <a class="home-button home-button--outline-dark" href="https://www.curseforge.com/minecraft/mc-mods/controlify">
            CurseForge
            <span aria-hidden="true">↗</span>
          </a>
          <a class="home-button home-button--outline-dark" href="/users/controller-support/">
            Setup docs
            <span aria-hidden="true">→</span>
          </a>
        </div>
      </div>
    </section>
  </main>
</template>
