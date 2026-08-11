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
      { text: 'Reference', link: '/reference/builtin-bindings' },
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
          { text: 'Default Binds', link: '/resource-packs/default-binds' },
          { text: 'Input Glyphs', link: '/resource-packs/input-glyphs' },
          { text: 'Button Guides', link: '/resource-packs/guides' },
          { text: 'Keyboard Layouts', link: '/resource-packs/keyboard-layouts' },
          { text: 'Radial Menu Icons', link: '/resource-packs/radial-icons' },
          { text: 'Adaptive Trigger Effects', link: '/resource-packs/adaptive-trigger-effects' }
        ]
      },
      {
        text: 'Developers',
        collapsed: false,
        items: [
          { text: 'Getting Started', link: '/developers/getting-started' },
          { text: 'Controlify Entrypoint', link: '/developers/controlify-entrypoint' },
          { text: 'Bindings API', link: '/developers/bindings-api' },
          { text: 'Screen Operation API', link: '/developers/screen-operation-api' },
          { text: 'Guides API', link: '/developers/guide-api' },
          { text: 'Adaptive Trigger API', link: '/developers/adaptive-trigger-api' }
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
          { text: 'Built-in Bindings', link: '/reference/builtin-bindings' },
          { text: 'Built-in Inputs', link: '/reference/builtin-inputs' },
          { text: 'Controller Namespaces', link: '/reference/builtin-controller-namespaces' }
        ]
      },
      {
        text: 'Architecture',
        items: [{ text: 'Controllers', link: '/architecture/controllers' }]
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
