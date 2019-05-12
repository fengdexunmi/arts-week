# Practice in vim 技巧1-6

本文章内容来自于《Vim实用技巧》一书。

## 技巧1 结识 `.`命令

> `.`命令可以让我们重复上次的修改，它是Vim中最为强大的多面手。

使用以下文本进行说明
```
Line one
Line two
Line three
Line four

```

`x` 命令可以删除光标下的字符
粗体表示当前的光标所在的位置

按键操作|缓冲区内容
--|--
开始| **L**ine one</br>Line two</br>Line three</br>Line four
`x`|**i**ne one</br>Line two</br>Line three</br>Line four
`.`|**n**e one</br>Line two</br>Line three</br>Line four
`..`|**空格**one</br>Line two</br>Line three</br>Line four

我们可以输入几次`u`撤销上述修改，使文档恢复到初始状态。

`dd`命令也做删除操作，但它会把整行一起删掉。

--|--
开始| **L**ine one</br>Line two</br>Line three</br>Line four
`dd`|**L**ine two</br>Line three</br>Line four
`.`|**L**ine three</br>Line four

**`.`命令是一个微型的宏**，我们可以录制任意数组的按键操作，然后在以后重复执行它们。

## 技巧2 不要自我重复

对于在行尾添加内容这样的常见操作，如添加分号，Vim提供了一个专门的命令，可以把两步操作合并为一步。

```
var foo = 1
var bar = 'a'
var foobar = foo + bar

```
我们想在上述文本的每行结尾添加一个分好。要实现这一点，先得把光标移动到行尾，然后切换到插入模式进行修改。`$`命令移动到行尾，接着执行`a;`，再按下<Esc>键完成修改。

### 减少无关的移动

`a`命令在当前光标之后添加内容，而`A`命令则在当前行的结尾添加内容。不管光标当前处于什么位置，输入`A`都会进入插入模式，并把光标移动到行尾。换句话说，`A`=`$a`。

## 技巧3 以退为进

我们平时可能会需要在一个字符前面各加一个空格。比如：

```
var foo = "method("+argument1+","+argument2+")";
```
在所有+号前后各加一个空格，可以这样完成：

按键操作|缓冲区内容
-|-
初始|**v**ar foo = "method("+argument1+","+argument2+")";
`f+`|var foo = "method("**+**argument1 + "," + argument2 + ")";
s␣+␣[Esc]|var foo = "method(" + argument1+","+argument2+")";
`;`|var foo = "method(" + argument1**+**","+argument2+")";
`.`|var foo = "method(" + argument1 + ","+argument2+")";
`;.`|var foo = "method(" + argument1 + "," + argument2+")";
`;.`|var foo = "method(" + argument1 + "," + argument2 + ")";

**`;`命令会重复查找上次`f`命令所查找的字符。**

## 技巧4 执行、重复、回退

可重复的操作及如何回退

目的|操作|重复|回退
-|-|-|-
做出一个修改|[edit]|`.`|`u`
在行内查找下一个指定字符|f{char}/t{char}|`;`|`,`
在行内查找上一个指定字符|F{char}/T{char}|`;`|`,`
在文档中查找下一处匹配项|/pattern<Enter>|`n`|`N`
在文档中查找上一处匹配项|?pattern<Enter>|`n`|`N`
执行替换|:s/tartget/replacement|`&`|`u`
执行一系列修改|`qx{changes}q`|`@x`|`u`

## 技巧5 查找并手动替换

word替换成newword

命令|目的
-|-
`/word`|查找
`*`|可能会出现两个结果：</br>一是光标会跳到下一个匹配项上；</br>二是所有出现这个单词的地方都会被高亮显示。如果没有看到高亮，试着运行一下:set hls
`cw`[newword]<Esc>|word替换成newword
`n`|下一个匹配项
`.`|重复上述修改

如果我们全局替换所有的，可以使用`:%s/word/newword/g`。

## 技巧6 结识 `.`范式

上述操作，都有一个共同点：

**用一键移动，另一键执行**

我们称为**`.`范式**
