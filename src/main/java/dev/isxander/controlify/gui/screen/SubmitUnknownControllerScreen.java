package dev.isxander.controlify.gui.screen;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.controller.ControllerType;
import dev.isxander.controlify.hid.HIDDevice;
import dev.isxander.controlify.utils.Log;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.WarningScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Pattern;

public class SubmitUnknownControllerScreen extends WarningScreen implements CustomWarningTitlePadding {
    public static final String SUBMISSION_URL = "https://api-controlify.isxander.dev/api/v1/submit";
    public static final Pattern NAME_PATTERN = Pattern.compile("^[\\w\\- ]{3,32}$");

    private final Controller<?, ?> controller;

    private final Screen lastScreen;

    private int invalidNameTicks;

    private Button submitButton;
    private EditBox nameField;

    public SubmitUnknownControllerScreen(Controller<?, ?> controller, Screen lastScreen) {
        super(
                Component.translatable("controlify.controller_submission.title").withStyle(ChatFormatting.BOLD),
                Component.translatable("controlify.controller_submission.message"),
                Component.translatable("controlify.controller_submission.dont_show_again"),
                Component.translatable("controlify.controller_submission.title")
        );
        if (!canSubmit(controller))
            throw new IllegalArgumentException("Controller ineligible for submission!");
        this.controller = controller;
        this.lastScreen = lastScreen;
    }

    @Override
    protected void initButtons(int textHeight) {
        this.submitButton = this.addRenderableWidget(
                Button.builder(Component.translatable("controlify.controller_submission.submit"), this::onSubmitButton)
                        .pos(this.width / 2 - 155, textHeight + 80)
                        .width(150)
                        .build()
        );
        this.addRenderableWidget(
                Button.builder(CommonComponents.GUI_CANCEL, btn -> onClose())
                        .pos(this.width / 2 + 5, textHeight + 80)
                        .width(150)
                        .build()
        );
        this.nameField = this.addRenderableWidget(
                new EditBox(
                      font,
                      this.width / 2 - 155,
                      textHeight + 105,
                      310, 20,
                      Component.translatable("controlify.controller_submission.name_narration")
                )
        );
        this.nameField.setHint(Component.translatable("controlify.controller_submission.name_hint"));
        this.nameField.setValue(controller.name());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics);

        super.render(graphics, mouseX, mouseY, delta);

        if (invalidNameTicks > 0) {
            graphics.drawCenteredString(font, Component.translatable("controlify.controller_submission.invalid_name").withStyle(ChatFormatting.RED), this.width / 2, nameField.getRectangle().bottom() + 4, -1);
        }
    }

    @Override
    public void tick() {
        if (invalidNameTicks > 0) {
            invalidNameTicks--;
            submitButton.active = invalidNameTicks < 50;
        }
    }

    protected void onSubmitButton(Button button) {
        invalidNameTicks = 0;

        if (!checkValidName()) {
            invalidNameTicks = 100;
            return;
        }

        if (submit()) {
            dontShowAgain();
            onClose();
        } else {
            // TODO: Show error message
            onClose();
        }
    }

    protected boolean submit() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(SUBMISSION_URL))
                    .POST(HttpRequest.BodyPublishers.ofString(this.generateRequestBody()))
                    .header("Content-Type", "application/json")
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() / 100 != 2) {
                Log.LOGGER.error("Received non-2xx status code from '{}', got {} with body '{}'", SUBMISSION_URL, response.statusCode(), response.body());
                return false;
            }

            Log.LOGGER.info("Successfully sent controller information to '{}'", SUBMISSION_URL);
            return true;
        } catch (Exception e) {
            Log.LOGGER.error("Failed to submit controller to '%s'".formatted(SUBMISSION_URL), e);
            return false;
        }
    }

    private String generateRequestBody() {
        HIDDevice hid = controller.hidInfo().orElseThrow().hidDevice().orElseThrow();

        JsonObject object = new JsonObject();
        object.addProperty("vendorID", hid.vendorID());
        object.addProperty("productID", hid.productID());
        object.addProperty("GUID", GLFW.glfwGetJoystickGUID(controller.joystickId()));
        object.addProperty("reportedName", nameField.getValue());
        object.addProperty("controlifyVersion", FabricLoader.getInstance().getModContainer("controlify").orElseThrow().getMetadata().getVersion().getFriendlyString());

        Gson gson = new Gson();
        return gson.toJson(object);
    }

    private boolean checkValidName() {
        String name = nameField.getValue().trim();
        return NAME_PATTERN.matcher(name).matches();
    }

    private void dontShowAgain() {
        controller.config().dontShowControllerSubmission = true;
        Controlify.instance().config().setDirty();
    }

    @Override
    public void onClose() {
        if (stopShowing.selected()) {
            dontShowAgain();
        }

        Controlify.instance().config().saveIfDirty();
        minecraft.setScreen(lastScreen);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    protected int getLineHeight() {
        return 9;
    }

    @Override
    public int getMessageY() {
        return 50;
    }

    public static boolean canSubmit(Controller<?, ?> controller) {
        return controller.type() == ControllerType.UNKNOWN
                && !controller.config().dontShowControllerSubmission
                && controller.hidInfo()
                        .map(info -> info.hidDevice().isPresent())
                        .orElse(false);
    }
}
