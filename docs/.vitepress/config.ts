import { defineConfig } from 'vitepress'
import { readdirSync } from "node:fs";
import { basename, extname } from "node:path";
import type { LanguageInput } from '@shikijs/types'
import mcfunctionGrammar from './grammars/mcfunction.tmLanguage.json' with { type: 'json' }

const changelogEntries = readdirSync("./content/changelog")
	.filter(fileName => extname(fileName) === ".md")
	.filter(fileName => fileName !== "index.md")
	.sort((first, second) =>
		second.localeCompare(first, undefined, { numeric: true })
	)
	.map(fileName => {
		const version = basename(fileName, ".md");

		return {
			text: version,
			link: `/changelog/${version}`,
		};
	});

export default defineConfig({
  srcDir: 'content',
  vite: { publicDir: '../public' },
  title: 'Controlify',
  description: 'Complete controller support for Minecraft: Java Edition.',
  cleanUrls: true,
  lastUpdated: true,
  markdown: {
    languages: [
      'java',
      'json',
      'json5',
      {
        ...mcfunctionGrammar,
        name: 'mcfunction',
        aliases: ['mcfunc']
      } as unknown as LanguageInput
    ]
  },
  head: [
    ['link', { rel: 'icon', type: 'image/png', sizes: '256x256', href: '/icon-256-bg.png' }],
    ['meta', { name: 'theme-color', content: '#fafd9e' }],
    ['meta', { property: 'og:image', content: '/icon-256-bg.png' }]
  ],
  themeConfig: {
    logo: '/icon-256-bg.png',
    siteTitle: 'Controlify',
    search: { provider: 'local' },
    nav: [
      { text: 'Players', link: '/users/controller-support' },
      { text: 'Resource Packs', link: '/resource-packs/custom-controller-identification' },
      { text: 'Developers', link: '/developers/getting-started' },
      { text: 'Reference', link: '/reference/builtin-actions' },
      {
        text: 'Download',
        items: [
          { text: 'Modrinth', link: 'https://modrinth.com/mod/controlify' },
          { text: 'CurseForge', link: 'https://www.curseforge.com/minecraft/mc-mods/controlify' }
        ]
      }
    ],
    sidebar: [
      {
        text: 'Players',
        collapsed: false,
        items: [
          {
						text: 'Controller Support',
						link: '/users/controller-support',
						items: [
							{ text: "Playstation Controllers", link: '/users/controller-support/playstation-controllers' },
							{ text: 'Valve Hardware', link: '/users/controller-support/valve-hardware' }
						]
					},
          { text: 'Mod Comparison', link: '/users/mod-comparison' }
        ]
      },
      {
        text: 'Resource Packs',
        collapsed: false,
        items: [
          { text: 'Controller Identification', link: '/resource-packs/custom-controller-identification' },
          { text: 'Joystick Mappings', link: '/resource-packs/joystick-mappings' },
          { text: 'Input Glyphs', link: '/resource-packs/input-glyphs' },
          { text: 'Keyboard Layouts', link: '/resource-packs/keyboard-layouts' },
          // these formats are documented alongside the API they serve
          { text: 'Contextual Facts', link: '/developers/contextual-api#adding-facts' },
          { text: 'Button Guides', link: '/developers/contextual-api#button-guides' },
          { text: 'Adaptive Trigger Effects', link: '/developers/contextual-api#adaptive-trigger-effects' },
          { text: 'Activators', link: '/developers/actions#activators' },
          { text: 'Default Binds', link: '/developers/actions#default-bindings' },
          { text: 'Radial Menu Icons', link: '/developers/actions#radial-menu-icon' },
          { text: 'Feedback Effects', link: '/developers/feedback-api#effects' },
          { text: 'Feedback Rules', link: '/developers/feedback-api#selecting-effects-with-rules' },
          { text: 'Feedback Groups', link: '/developers/feedback-api#groups-and-ordering' }
        ]
      },
      {
        text: 'Developers',
        collapsed: false,
        items: [
          { text: 'Getting Started', link: '/developers/getting-started' },
          { text: 'Controlify Entrypoint', link: '/developers/controlify-entrypoint' },
          { text: 'Actions', link: '/developers/actions' },
          { text: 'Screen Operation API', link: '/developers/screen-operation-api' },
          { text: 'Contextual API', link: '/developers/contextual-api' },
          { text: 'Feedback', link: '/developers/feedback-api' },
          { text: 'Look API', link: '/developers/look-api' },
          { text: 'Controllers', link: '/developers/controllers' }
        ]
      },
      {
        text: 'Upgrading',
        collapsed: false,
        items: [
          { text: 'Migrating to v4', link: '/developers/migrating-to-v4' }
        ]
      },
			{
				text: 'Changelog',
				collapsed: true,
				items: changelogEntries
			},
      {
        text: 'Reference',
        collapsed: false,
        items: [
          { text: 'Built-in Actions', link: '/reference/builtin-actions' },
          { text: 'Built-in Inputs', link: '/reference/builtin-inputs' },
          { text: 'Controller Namespaces', link: '/reference/builtin-controller-namespaces' },
          // these tables live on the pages for the API they belong to
          { text: 'Action Layers', link: '/developers/actions#built-in-layers' },
          { text: 'Activator Types', link: '/developers/actions#activators' },
          { text: 'Gestures', link: '/developers/actions#gestures' },
          { text: 'Feedback Events', link: '/developers/feedback-api#controlify-s-events' },
          { text: 'Ambient Feedback Params', link: '/developers/feedback-api#ambient-params' },
          { text: 'In-game State', link: '/developers/contextual-api#in-game' },
          { text: 'Container State', link: '/developers/contextual-api#container' },
          { text: 'Trigger Effect Types', link: '/developers/contextual-api#effect-types' },
          { text: 'Element Behaviours', link: '/developers/screen-operation-api#using-a-built-in-behaviour' },
          { text: 'Virtual Mouse Modes', link: '/developers/screen-operation-api#virtual-mouse' },
          { text: 'Registration Events', link: '/developers/controlify-entrypoint#registration-events' },
          { text: 'State Events', link: '/developers/controlify-entrypoint#state-events' }
        ]
      }
    ],
    outline: { level: [2, 3], label: 'On this page' },
    editLink: {
      pattern: 'https://github.com/isXander/Controlify/edit/multiversion/dev/docs/content/:path',
      text: 'Edit this page on GitHub'
    },
    socialLinks: [
      { icon: 'github', link: 'https://github.com/isXander/Controlify' },
      { icon: 'discord', link: 'https://short.isxander.dev/discord' }
    ],
    footer: {
      message: 'Controlify is an open-source Minecraft mod.',
      copyright: 'Made with care by isXander and contributors.'
    },
    lastUpdated: { text: 'Updated' },
    docFooter: { prev: 'Previous', next: 'Next' }
  }
})
