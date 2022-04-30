# WebAssembly Interpreter on Java

## ビルド方法

```shell
mvnw install
```

## 仕様書

- [WebAssembly Reference Manual](https://github.com/sunfishcode/wasm-reference-manual/blob/master/WebAssembly.md)
  - 非公式の読みやすい仕様解説ドキュメント
- [MDN WebAssembly instructions](https://developer.mozilla.org/en-US/docs/WebAssembly/Reference)
  - 豊富なWATサンプル


- [WebAssembly Core Specification 1.0](https://www.w3.org/TR/wasm-core-1/)
- [WebAssembly Core Specification 2.0](https://www.w3.org/TR/wasm-core-2/)
- [WebAssembly Core Specification (Draft)](https://webassembly.github.io/spec/core/)


- [WEBASSEMBLY USUI BOOK](https://ukyo.github.io/wasm-usui-book/webroot/binary-format.html)
  - 日本語の仕様解説

## テストスイート

- [本家テストスイート](https://github.com/WebAssembly/spec/tree/main/test/core)
  - [WABTのwast2jsonコマンドでwasmファイルを生成](https://github.com/WebAssembly/wabt)
- [各種仕様を集約したテストスイート](https://github.com/WebAssembly/testsuite)

## 変換ツール

- [WABT: The WebAssembly Binary Toolkit](https://github.com/WebAssembly/wabt)
  - [Webでwatからwasmに変換(実装が古め)](https://webassembly.github.io/wabt/demo/wat2wasm/)
  - [Webでwasmからwatに変換](https://webassembly.github.io/wabt/demo/wasm2wat/)
