name: Controller compatibility request
description: Get your controller to be detected out-of-box by Controlify
title: "[Controller Request] <Controller name here>"
labels: ["controller-compat"]
body:
  - type: input
    id: controller-name
    attributes:
      label: Controller name
      description: The full name/model of your controller
      placeholder: e.g. Xbox Wireless Controller (Model 1914)
    validations:
      required: true

  - type: input
    id: vid-pid
    attributes:
      label: Controller VID & PID
      description: Vendor ID and Product ID (found in Device Manager on Windows, `lsusb` on Linux, or System Information on macOS)
      placeholder: e.g. VID_045E & PID_02FD
    validations:
      required: true

  - type: input
    id: controlify-version
    attributes:
      label: Version of Controlify
      placeholder: e.g. 2.1.0
    validations:
      required: true

  - type: input
    id: minecraft-version
    attributes:
      label: Version of Minecraft
      placeholder: e.g. 1.21.1
    validations:
      required: true

  - type: input
    id: os-version
    attributes:
      label: OS & version
      placeholder: e.g. Windows 11 23H2 / Ubuntu 24.04 / macOS Sonoma 14.5
    validations:
      required: true
