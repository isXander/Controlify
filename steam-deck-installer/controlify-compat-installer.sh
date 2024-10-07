#!/bin/bash
echo "#######################################################"
echo "## Steam Deck compatibility for Controlify Installer ##"
echo "#######################################################"

DEBUG_FILE="/home/deck/.steam/steam/.cef-enable-remote-debugging"

# Check if the debug file is already there
if [ -f "${DEBUG_FILE}" ]; then
    echo "You don't need to run this script, the Steam features required for Controlify are already enabled. You probably have Decky installed."
    echo "Exiting..."
    sleep 5
    exit 0
fi

echo "!! PLEASE READ THE FOLLOWING CAREFULLY BEFORE CONTINUING: !!"
echo ""
echo "This script will enable a debugging feature in Steam to allow Controlify to receive the custom inputs such as gyro, back buttons and touch-pads."
echo "This is a very minimal process, and will not affect your Steam Deck in any way."
echo ""
# shellcheck disable=SC2162
read -p "Press 'A'/Enter to continue the installation process, close the Konsole window to exit: "

echo "Adding .cef-enable-remote-debugging file to Steam directory..."

# Enable the remote debugging for steam to allow Controlify to interface with it
touch "${DEBUG_FILE}"

FLATPAK_DIR="/home/deck/.var/app/com.valvesoftware.Steam/data/Steam"
# if installed as flatpak, put .cef-enable-remote-debugging in there
[ -d "${FLATPAK_DIR}" ] && touch "${FLATPAK_DIR}/.cef-enable-remote-debugging"

echo "Restarting SteamWebHelper..."
# Restart Steam to apply the changes
killall -s SIGTERM steamwebhelper

sleep 10 # wait for steam to restart

echo "Done! Return to gaming mode and launch Controlify to enjoy Minecraft on your Deck."
sleep 5

