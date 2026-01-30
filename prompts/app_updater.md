请你阅读这个项目的github workflow，它会打包这个APP。
我想实现一个应用更新的功能，需要你给我实现方案，首先声明我不使用第三方服务。
我的想法是将打包后的apk由workflow上传至Cloudflare r2，这样用户可以在全球访问这个安装包，借助CDN，费用能降至最低。
然后就是应用的更新问题，我想尽量让用户的更新无感，但是APP不上架商店，完全无感是不可能的，但是尽量减少安装的步骤。
我需要你给我一些方案，如果有简化实现的第三方库或者工具就更好了。

---

app/src/test/java/com/example/jitterpay/scheduler/UpdateSchedulerTest.kt,app/src/test/java/com/example/jitterpay/ui/update/UpdateViewModelTest.kt,app/src/test/java/com/example/jitterpay/util/UpdateManagerTest.kt,阅读这三个文件，并寻找项目的APP更新实现，充分阅读并熟悉，先对你整个APP的更新流程的理解进行总结。
总结之后你需要分析这些测试是否能够包含APP更新的必要测试，因为APP真正的发布不可能为了测试一下发布功能是否真正而发布一个版本的，这是不允许的，所以我需要你提出一些解决方案，或许你也可以通过搜索来查找成熟的解决方案，总之APP的更新功能不能通过发布新版本来测试，但是又要确保其功能正常。