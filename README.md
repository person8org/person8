# Person8
## A Lifeline for digital nomads and others at risk of loosing their identity...

[![Netlify Status](https://api.netlify.com/api/v1/badges/0ee6baf5-bfde-4f7a-ac0d-d4856b61677e/deploy-status)](https://app.netlify.com/sites/person8/deploys)

![Pietron Logo](https://pietron.app/media/logo.png)

### [Support us with a slice on Pietron!](https://pietron.app/new?name=Person8&address=3QZd2cvtAod5EJuCA1S726RTjnTTrTTp8U)

## Setup

```shell
yarn install || npm install
lein do clean, deps, compile
```

#### Transpile JSX with Babel
```shell
npm run build
```

    "babel-plugin-css-modules-transform": "^1.6.2",
    "babel-plugin-transform-import-css": "0.1.6",
    "babel-plugin-transform-react-jsx": "^6.24.1",


    "babel-plugin-react-css-modules": "3.4.2",

    "babel-cli": "^6.20.0",

"transform-react-jsx",
["transform-import-css", {
"generateScopedName": "lib-[folder]-[name]-[local]-[hash:base64:4]"
}],
"react-css-modules",

 -d ./src/gen/ --copy-files

#### Run dev server
```shell
yarn dev || npm run dev
```

(require '[shadow.cljs.devtools.server :as server])
(require '[shadow.cljs.devtools.api :as shadow])
(server/start!)

(shadow/watch :app)
(shadow/nrepl-select :app)


#### Compile an optimized version

```shell
yarn release || npm run release
```
