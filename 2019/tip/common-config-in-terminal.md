# Terminal常用配置以及使用
Terminal是我们开发过程常用的一个工具。高效利用Terminal能够提高工作效率。
下面是我在开发过程中的一些小技巧，可能很多你已经在使用了。

下面的介绍都是在Mac环境下进行的。

## zsh
我推荐大家配置zsh为默认的shell环境。我安装的是[Oh My Zsh](https://github.com/robbyrussell/oh-my-zsh)。

> Oh My Zsh is an open source, community-driven framework for managing your zsh configuration. It comes with a bunch of features out of the box and improves your terminal experience.

### cdf
当我们打开Finder（或者再继续点击目录），这个时候在Terminal中输入`cdf`命令并回车。

我们可以看到已经跳到了我们打开的这个目录，是不是很方便。
```
➜  ~ pwd
/Users/frank
➜  ~ cdf
➜  themes 
➜  themes pwd
/Users/frank/BlogProject/github/themes
```

### 目录跳转
```
➜  github pwd
/Users/frank/BlogProject/github
```
比如github文件夹目录所在目录为~/BlogProject/github，我们想跳转到github可以在~(即/Users/frank)目录下，先输入cd B/g，然后按下tab键自动补全，即会变成完整的 cd BlogProject/github/，当然如果有多个B和g开头的文件夹，就会让我们选择哪一个文件夹，也挺方便的。
```
➜  ~ 
➜  ~ cd B/g 
```

### alias
在zsh的配置文件（~/.zshrc）中配置alias别名，对于常用的命令，可以使用alias简化命令。

```
alias gst='git status'
alias gcm='git commit -m '
alias gps='git push'
```

根据自己需要扩展更多的别名。

## git
git全局配置代理

```
git config --global https.proxy http://127.0.0.1:1080

git config --global https.proxy https://127.0.0.1:1080

git config --global --unset http.proxy

git config --global --unset https.proxy
```

只想在这一次git clone代码的时候使用代理

git clone `origin url` --config 'http.proxy=127.0.0.1:1080'

（<small>ip地址和端口号根据自己的实际去配置</small>）







