# Understanding Clean Code in Android
原文链接 https://medium.com/mindorks/understanding-clean-code-in-android-ebe42ad89a99

**「理解Android中的代码简洁之道」**
，在写代码之前，最好理解如何管理自己的代码以及如何写出具有扩展性的代码。

正如Uncle Bob在书中所说,
> You are reading this “article” for two reasons. First, you are a programmer. Second, you want to be a better programmer.<br> — Robert C. Martin

## 什么是‘简洁的代码’？
如果团队中每一位成员都可以很容易看懂你的代码，那么你的代码就称得上‘简洁’。简洁的代码可供其他的开发者阅读和扩展，除了代码的**可理解性**还需要关注它的**可读性**、**可变性**、**可扩展性**以及**可维护性**。

## 简洁的代码的特征
- **代码应该是优雅的**  
就像精心设计的音乐盒或者巧妙构思的车辆一样，优雅的代码也可以让你笑逐颜开。
- **代码应该是被考虑周全的**  
保持代码的简洁和条理性，需要注重代码的细节。
- **代码应该是单一性**  
每一个功能、类和模块，应该具有单一性，集中处理一件事情，不受其他细节干扰。
- **不包含重复逻辑**
- **通过所有测试**
- **拥有最小化的类、方法、功能或者其他**

## 有意义的命名
好的命名（类、方法、遍历）应该是对要解决的问题的回答。从命名中可以看出它为什么存在、它是做什么的以及应该如何使用它。

例如:
```Kotlin
// Bad variables naming
var a = 0 // user ages
var w = 0 // user weight
var h = 0 // user height


// Bad functions naming
fun age()
fun weight()
fun height()


// Bad classes naming to get user data
class UserInfo()


// Best practices varibales naming
var userAge = 0
var userWeight = 0
var userHeight = 0


// Best practices functions naming
fun setUserAge()
fun setUserWeight()
fun setUserHeight()


// Best practices classes naming to get user data
class Users()
```

- **类的命名**  
类或者对象命名应该用名词或者名词短语。比如**Customer**, **WikiPage**, **Account**, 和**AddressParser**。
避免使用Manager, Processor, Data, 或者 Info。
- **方法的命名**  
方法的命名应该用动词或者动词短语。比如postPayment, deletePage, 或者 save。
- **使用问题域命名**  

## 使用[SOLID](https://zh.wikipedia.org/wiki/SOLID_(%E9%9D%A2%E5%90%91%E5%AF%B9%E8%B1%A1%E8%AE%BE%E8%AE%A1))原则编写代码
> SOLID（单一功能、开闭原则、里氏替换、接口隔离以及依赖反转）是由罗伯特·C·马丁在21世纪早期[1] 引入的记忆术首字母缩略字[2][3]，指代了面向对象编程和面向对象设计的五个基本原则。

### Single Responsibility Principle-SRP
单一职责，每一个类应该有单一的职责，避免出现上帝类（A "God Class" is an object that controls way too many other objects in the system and has grown beyond all logic to become The Class That Does Everything.）。

```Kotlin
class MyAdapter(val friendList: List<FriendListData.Friend>) :
    RecyclerView.Adapter<CountryAdapter.MyViewHolder>() {

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var name: TextView = view.findViewById(R.id.text1)
        var popText: TextView = view.findViewById(R.id.text2)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val friend = friendList[position]
        
        val status = if(friend.maritalStatus == "Married") {
            "Sold out"
        } else {
            "Available"
        }
        
        holder.name.text = friend.name
        holder.popText.text = friend.email
        holder.status.text = status
    }

    override fun getItemCount(): Int {
        return friendList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_friendlist, parent, false)
        return MyViewHolder(view)
    }
}
```
上述代码，在RecyclerView.Adapter的onBindViewHolder中处理了业务逻辑，严格来讲这不符合单一职责，因为onBindViewHolder只负责实现数据和视图的绑定。

### Open-Closed Principle-OCP
开闭原则，对扩展开放，对修改关闭。对于A类，我们想要修改A类的一个方法，我们可以通过继承A类来重写方法而不是直接去修改A类的方法。
比如我们可以继承RecyclerView.Adapter类来自定义Adapter，实现想要的行为。
```Kotlin
class FriendListAdapter(val friendList: List<FriendListData.Friend>) :
    RecyclerView.Adapter<CountryAdapter.MyViewHolder>() {

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var name: TextView = view.findViewById(R.id.text1)
        var popText: TextView = view.findViewById(R.id.text2)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val friend = friendList[position]
        holder.name.text = friend.name
        holder.popText.text = friend.email
    }

    override fun getItemCount(): Int {
        return friendList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_friendlist, parent, false)
        return MyViewHolder(view)
    }
}

```
### Liskov Substitutions Principle-LSP
子类不应该破坏父类的类型定义。

里氏替换原则，子类重写父类的方法时候不要破坏父类的原有功能。尽量不要从可实例化的父类中继承，而是要使用基于抽象类和接口的继承。
比如，定义了一个接口，接口里面有一个方法onClick(),Activity实现这个接口，则要重写onClick()方法。
```Kotlin
interface ClickListener {
    fun onClick()
}

class MyActivity: AppCompatActivity(), ClickListener {

    //........
    override fun onClick() {
        // Do the magic here
        toast("OK button clicked")
    }

}
```
### Interface Segregation Principle-ISP
接口隔离原则，A类实现B类的时候，只需要重写自己需要的方法，而不是重写所有方法。
比如，我们实现SearchView.OnQueryTextListener()接口，只想用onQuerySubmit()，我们可以这样做。
```Kotlin
interface SearchViewQueryTextCallback {
    fun onQueryTextSubmit(query: String?)
}

class SearchViewQueryTextListener(val callback: SearchViewQueryTextCallback): SearchView.OnQueryTextListener {
    override fun onQueryTextSubmit(query: String?): Boolean {
        callback.onQueryTextSubmit(query)
        return true
    }

    override fun onQueryTextChange(query: String?): Boolean {
        return false
    }
}
```
在View中如何实现：
```
val listener = SearchViewQueryTextListener(
    object : SearchViewQueryTextCallback {
        override fun onQueryTextSubmit(query: String?) {
             // Do the magic here
        } 
    }
)
mSearchView.setOnQueryTextListener(listener)
```

如果使用Kotlin的方法扩展，可以这样实现。
```Kotlin
interface SearchViewQueryTextCallback {
    fun onQueryTextSubmit(query: String?)
}

fun SearchView.setupQueryTextSubmit (callback: SearchViewQueryTextCallback) {
    setOnQueryTextListener(object : SearchView.OnQueryTextListener{
        override fun onQueryTextSubmit(query: String?): Boolean {
            callback.onQueryTextSubmit(query)
            return true
        }

        override fun onQueryTextChange(query: String?): Boolean {
            return false
        }
    })
}
```
如何在View中实现：
```
val listener = object : SearchViewQueryTextCallback {
    override fun onQueryTextSubmit(query: String?) {
        // Do the magic here
    }
}
mSearchView.setupQueryTextSubmit(listener)
```
Koltin的方法扩展确实方便~~

### Dependence Inversion Principle-DIP
依赖倒置原则，依赖于抽象，不要依赖于具体。  

- 高层级模块不应该依赖低层级模块。
- 抽象不要依赖于细节，细节应该依赖于抽象。

高层级模块包含着复杂的逻辑，它是可复用的，不应该受提供公共特性的低层级模块影响。
比如在MVP模式中，对象接口帮助我们与具体类进行通讯。UI类（Activity/Fragment）不需要知道Presenter的具体方法实现，UI类不知道也不关心Presenter的变化。

比如：
```Kotlin
interface UserActionListener {
    fun getUserData()
}

class UserPresenter : UserActionListener() {
    // .....
  
    override fun getUserData() {
        val userLoginData = gson.fromJson(session.getUserLogin(), DataLogin::class.java)
    }
  
    // .....
}
```
对于的Activity实现如下：
```Kotlin
class UserActivity : AppCompatActivity() {
   
   //.....
   val presenter = UserPresenter()
   
   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      
      // Activity doesn't need to know how presenter works
      // for fetching data, it just know how to call the functions
      // So, if you add method inside presenter, it won't break the UI.
      // even the UI doesn't call the method.
      
      presenter.getUserData()
   }
   
   //....
}
```
Activity不需要知道Presenter如何工作，只需要知道调用哪个方法。

## 总结
在编写代码的时候，我们要避免无意义的命名，上帝类以及面条式代码（难懂复杂的代码）的出现，遵循SOLID原则。

最后，Keep learning and keep sharing。
