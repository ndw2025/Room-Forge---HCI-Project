COMMIT INSTRUCTIONS - member-02-designer-workflow

Assigned Files
- app/src/main/java/com/furnituredesigner/client/ui/DesignerDashboardPanel.java
- app/src/main/java/com/furnituredesigner/client/ui/NavigationPanel.java
- app/src/main/java/com/furnituredesigner/client/ui/MainFrame.java

Branch Name
git checkout -b member02/designer-workflow

Commit Message
git commit -m "feat(designer): dashboard workflow and navigation"

Exact Git Sequence (Run in order)
git checkout main
git pull origin main
git checkout -b member02/designer-workflow
git config --local user.name "Your Name"
git config --local user.email "your-github-email@example.com"
git add .
git commit -m "feat(designer): dashboard workflow and navigation"
git push -u origin member02/designer-workflow

Open Pull Request
Option A (GitHub web):
Open: https://github.com/ORG/REPO/compare/member02/designer-workflow?expand=1
Set base branch to main, then create PR.

Option B (GitHub CLI):
gh pr create --base main --head member02/designer-workflow --title "feat(designer): dashboard workflow and navigation" --body "Member contribution commit for member-02-designer-workflow."

Notes
- Only commit files from this pack.
- Pull latest main before creating your branch.
- Do not push directly to main.
