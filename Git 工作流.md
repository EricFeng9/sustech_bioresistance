## Git 工作流

### 初始化

```sh
git clone https://github.com/EricFeng9/sustech_bioresistance.git
```

### 修改代码

1. **创建功能分支**
   ```sh
   git checkout -b my-feature
   ```

2. **对项目进行修改**

3. **添加修改到暂存区**

   ```sh
   git add .
   ```
   > `.` 表示将所有更改的文件添加到暂存区
   > 也可以指定特定文件路径

4. **提交修改**

   ```sh
   git commit -m "描述你的修改"
   ```

5. **推送到远程仓库**
   ```sh
   git push origin my-feature
   ```

6. **切换到 main 分支**

   ```sh
   git checkout main
   ```

7. **处理上游更改（如需要）**

   如果远程 main 分支已更新，需要将 my-feature 重新基于最新的 main：

   ```sh
   # 拉取最新更改
   git pull origin main
   
   # 切回功能分支
   git checkout my-feature
   
   # 将功能分支重新基于最新的 main
   git rebase main
   
   # 解决冲突（如有）后，强制推送更新的功能分支
   git push -f origin my-feature
   ```

8. **创建合并请求**
   - 在 Github 界面创建从 my-feature 到 main 的合并请求（New pull request）
   - 团队维护者审核后执行 "Squash and merge"，并删除功能分支

9. **清理本地分支**
   ```sh
   # 删除本地功能分支
   git branch -D my-feature
   
   # 拉取最新主分支
   git pull origin main
   ```