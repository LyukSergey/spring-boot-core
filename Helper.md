1) Якщо ви запушили .idea
git rm -r --cached .idea

2) доати новий проект в git якщо репозиторію немає
# 1. Розпакувати завантажений zip і зайти в папку проєкту
cd назва-проєкту
# 2. Ініціалізувати git (Spring Initializr вже кладе .gitignore у zip)
git init
# 3. Додати всі файли до індексу
git add .
# 4. Перший комміт
git commit -m "Initial commit" 
# 5. Створити репозиторій на GitHub через сайт (New repository),
#    БЕЗ README/.gitignore/license — вони вже є в проєкті
# 6. Прив'язати віддалений репозиторій (URL з GitHub)
git remote add origin https://github.com/<юзер>/<репо>.git  
# 7. Перейменувати гілку на main (git init інколи створює master)
git branch -M main
# 8. Запушити
git push -u origin main 