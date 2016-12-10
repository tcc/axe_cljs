# axe_cljs #

http://axe.g0v.tw 

斧頭幫 ClojureScript 解答



ClojureScript(cljs) 是 Clojure 能和 JavaScript 互通有無的版本。

cljs 原本和 Clojure 一樣, 都要在 JVM 上, 經過一些程序才能執行。

但去年的 planck , 到上個月的 lumo 都是可以獨立執行的版本, 就如同大多數腳本語言一般。

但程式庫的管理沒趕上大多數腳本語言的腳步, 所以在這裡儘量使用這些工具本身的功能。

因此 lv1, lv2 用 planck。

```shell
$ brew install planck
...
$ planck lv1.cljs
...
$ planck lv2.cljs
...
```



而 lv3 要用 cookie, lv4 要給 Header … 只好搬能夠和 npm 合作的 lumo。

(使用前, `請先 npm install request`)

```shell
$ brew install lumo
...
$ npm install request
...
$ lumo lv3.cljs
...
$ lumo lv4.cljs
...
```



// the end