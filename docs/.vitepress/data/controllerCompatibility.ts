export enum Controller {
  XInput = 'XInput-compatible controller',
  XboxEliteSeries2 = 'Xbox Elite Series 2',
  SonyDualSense = 'Sony DualSense',
  SteamDeck = 'Steam Deck',
  GoogleStadia = 'Google Stadia',
  SteamController = 'Steam Controller'
}

export enum Connection {
  NotApplicable = 'Not applicable',
  USB = 'USB',
  Bluetooth = 'Bluetooth',
  BuiltIn = 'Built in'
}

export enum OperatingSystem {
  Windows = 'Windows',
  Linux = 'Linux',
  MacOS = 'macOS',
  SteamOS = 'SteamOS'
}

export enum SupportStatus {
  Works = 'works',
  SetupRequired = 'setup-required',
  Partial = 'partial',
  Unsupported = 'unsupported',
  NotApplicable = 'not-applicable'
}

export const compatibilityFeatures = [
  'Standard input',
  'Rumble',
  'Extra inputs',
  'Gyro',
  'Touchpad',
  'HD haptics',
  'Adaptive triggers'
] as const

export type FeatureSupport = readonly [
  standardInput: SupportStatus,
  rumble: SupportStatus,
  extraInputs: SupportStatus,
  gyro: SupportStatus,
  touchpad: SupportStatus,
  hdHaptics: SupportStatus,
  adaptiveTriggers: SupportStatus
]

export interface CompatibilityEntry {
  readonly controller: Controller
  readonly connection: Connection
  readonly operatingSystem: OperatingSystem
  readonly support: FeatureSupport
}

export const controllerGuides: Partial<Record<Controller, string>> = {
  [Controller.SonyDualSense]: '/users/controller-support/playstation-controllers',
  [Controller.SteamDeck]: '/users/controller-support/valve-hardware#steam-deck',
  [Controller.GoogleStadia]: '/users/controller-support/stadia-controller',
  [Controller.SteamController]: '/users/controller-support/valve-hardware#steam-controller-2026'
}

const { XInput, XboxEliteSeries2: Elite, SonyDualSense: DualSense, SteamDeck: Deck, GoogleStadia: Stadia } = Controller
const { NotApplicable: NoConnection, USB, Bluetooth, BuiltIn } = Connection
const { Windows, Linux, MacOS, SteamOS } = OperatingSystem
const { Works: Y, SetupRequired: S, Unsupported: N, NotApplicable: NA } = SupportStatus

const row = (
  controller: Controller,
  connection: Connection,
  operatingSystem: OperatingSystem,
  support: FeatureSupport
): CompatibilityEntry => ({ controller, connection, operatingSystem, support })

export const compatibilityMatrix = [
  row(XInput, NoConnection, Windows, [Y, Y, NA, NA, NA, NA, NA]),
  row(Elite, USB, Windows, [Y, Y, N, NA, NA, NA, NA]),
  row(Elite, USB, Linux, [Y, Y, Y, NA, NA, NA, NA]),
  row(Elite, USB, MacOS, [Y, Y, Y, NA, NA, NA, NA]),

  row(DualSense, USB, Windows, [Y, Y, Y, Y, Y, Y, Y]),
  row(DualSense, Bluetooth, Windows, [Y, Y, Y, Y, Y, N, Y]),
  row(DualSense, USB, Linux, [Y, Y, Y, Y, S, Y, Y]),
  row(DualSense, Bluetooth, Linux, [Y, Y, Y, Y, S, N, Y]),
  row(DualSense, USB, MacOS, [Y, Y, Y, Y, Y, N, Y]),
  row(DualSense, Bluetooth, MacOS, [Y, Y, Y, Y, Y, N, Y]),

  row(Deck, BuiltIn, SteamOS, [Y, Y, N, N, N, NA, NA]),
  row(Deck, BuiltIn, Windows, [Y, Y, Y, Y, Y, NA, NA]),

  row(Stadia, USB, Windows, [Y, Y, Y, NA, NA, NA, NA]),
  row(Stadia, USB, Linux, [Y, Y, Y, NA, NA, NA, NA]),
  row(Stadia, USB, MacOS, [Y, Y, Y, NA, NA, NA, NA]),
  row(Stadia, Bluetooth, Windows, [S, N, S, NA, NA, NA, NA]),
  row(Stadia, Bluetooth, Linux, [S, S, S, NA, NA, NA, NA]),
  row(Stadia, Bluetooth, MacOS, [S, S, S, NA, NA, NA, NA])
] as const satisfies readonly CompatibilityEntry[]
