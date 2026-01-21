interface WindowManager
WindowManager -> getDisplayImePolicy 
WindowManager -> setDisplayImePolicy

WindowManagerImpl implements WindowManager

WindowManagerImpl -> setDisplayImePolicy(int displayId, @DisplayImePolicy int imePolicy) -> WindowManagerGlobal.getWindowManagerService().setDisplayImePolicy

WindowManagerImpl -> getDisplayImePolicy(int displayId) -> WindowManagerGlobal.getWindowManagerService().getDisplayImePolicy(displayId);



scrcpy --new-display=1920x1080 --start-app com.nightmare.adbkit --display-ime-policy=local

仅仅会将新创建的虚拟显示器的 ImePolicy 置为 0，如果之前的主显示器和扩展显示器也为0
不影响输入法单独在新的虚拟显示器上弹出

但是，Neo 用同样的 flags 创建的虚拟显示器，为什么弹不出输入法？
目前猜测是焦点在多个显示器间移动导致的？

## 尝试解决

- 新创建虚拟显示器不单独设置 ime-policy, 默认值为 1


## 不使用扩展显示器，输入法也无法弹出



## 脏东西
- Flutter App 通过 Neo 打开，点击输入框无法打开键盘
- 其他 App 通过 Neo 打开，能打开键盘 
- ADB KIT Debug 模式，点输入框，键盘能弹，Release 模式不能弹，Neo Resize 窗口后，点击又能弹一次
- 用 scrcpy 显示对应的 display，点击输入框能弹
- 用 adb shell input -d 147 tap 300 300，点击 Neo 内的 Flutter App(ADB KIT)，能弹(关键)

目前结论还是因为焦点，手指按下的时候，有两个 Display 同时有焦点

有没可能还是焦点？

Code LFA 启动速度比较慢

现在有个复杂的情况如下

我使用 shell 权限创建了虚拟显示器

使用 options = ActivityOptions.makeBasic().setLaunchDisplayId(display.getDisplayId());
让activity 在指定的显示器上打开

目前的问题是

打开非 flutter 框架的 app，其中的输入框点击后，能弹出键盘

但是打开 flutter 的app，则不能弹起键盘


## 继续
在 Neo Activity 中加入以下代码
```java
getWindow().setFlags(
    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
);
```
这个时候，不管是启动 Flutter App 还是其他，通过 adb tap 或者 scrcpy 都无法弹起输入法

所以这个是不是依赖当前 Activity 的焦点？


scrcppy的指针鼠标可以出现在虚拟显示器上

用模拟器试试各个版本的情况

用 Pixso 画一个一模一样的光标

饿了 吃饭