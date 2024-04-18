from PIL import Image
import os

# Define the folder structure
base_folder = "./textures/gui/sprites/inputs"
output_folder = "./textures/font/controller"

# Define the dimensions of the sprite sheet
num_columns = 8
sprite_width = 22  # Change this to the width of your sprites
sprite_height = 22  # Change this to the height of your sprites

def create_sprite_sheet(theme_folder):
    theme_path = os.path.join(base_folder, theme_folder)
    sprites = []
    for group_folder in os.listdir(theme_path):
        group_path = os.path.join(theme_path, group_folder)
        if not os.path.isdir(group_path):
            continue
        for filename in os.listdir(group_path):
            if filename.endswith(".png"):
                sprite_path = os.path.join(group_path, filename)
                sprite = Image.open(sprite_path)
                sprites.append(sprite)
    
    # Calculate the number of rows needed based on the number of sprites
    num_sprites = len(sprites)
    num_rows = (num_sprites + num_columns - 1) // num_columns

    # Create a new blank image for the sprite sheet
    sheet_width = num_columns * sprite_width
    sheet_height = num_rows * sprite_height
    sprite_sheet = Image.new("RGBA", (sheet_width, sheet_height), (0, 0, 0, 0))

    # Paste each sprite onto the sprite sheet
    for i, sprite in enumerate(sprites):
        row = i // num_columns
        col = i % num_columns
        x_offset = col * sprite_width
        y_offset = row * sprite_height
        sprite_sheet.paste(sprite, (x_offset, y_offset))

    # Save the sprite sheet
    output_path = os.path.join(output_folder, theme_folder + ".png")
    sprite_sheet.save(output_path)

# Iterate over theme folders and create sprite sheets
for theme_folder in os.listdir(base_folder):
    if os.path.isdir(os.path.join(base_folder, theme_folder)):
        create_sprite_sheet(theme_folder)
