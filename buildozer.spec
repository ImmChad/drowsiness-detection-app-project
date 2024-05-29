[app]
title = Drowsiness App
package.name = drowsinessapp
package.domain = org.test
source.dir = .
source.include_exts = py,png,jpg,kv,atlas
version = 0.1
requirements = python3==3.7.6,hostpython3==3.7.6,cython==0.29.33,kivy,kivymd==1.1.1,pillow
# Thêm lệnh prebuild để áp dụng patch
p4a.hook.prebuild = python ./prebuild_patch.py
orientation = portrait
osx.python_version = 3.7.6
osx.kivy_version = 1.9.1
fullscreen = 1
android.archs = arm64-v8a, armeabi-v7a
android.allow_backup = True
ios.kivy_ios_url = https://github.com/kivy/kivy-ios
ios.kivy_ios_branch = master
ios.ios_deploy_url = https://github.com/phonegap/ios-deploy
ios.ios_deploy_branch = 1.10.0
ios.codesign.allowed = false
[buildozer]
log_level = 2
warn_on_root = 1
