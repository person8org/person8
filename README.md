# Person8

[![Netlify Status](https://api.netlify.com/api/v1/badges/0ee6baf5-bfde-4f7a-ac0d-d4856b61677e/deploy-status)](https://app.netlify.com/sites/person8/deploys)

## Based on shadow-cljs, proto-repl, reagent template

`shadow-cljs` is a build tool for ClojureScript.

`proto-repl` is a Clojure(Script) dev env for [Atom](https://atom.io/)

`reagent` is a ClojureScript wrapper for [React](https://reactjs.org/).

Shadow walk-through:

http://manuel-uberti.github.io/programming/2018/11/14/deps-shadow-cljs/


## Setup And Run
#### Copy repository
```shell
git clone https://github.com/jacekschae/shadow-reagent.git && cd shadow-reagent
```

#### Install dependencies
```shell
yarn install || npm install
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
