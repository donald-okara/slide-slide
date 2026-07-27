# Update Existing Issues with Labels and Milestones

The user has already created GitHub issues for the Slide Slide project but missed assigning labels and milestones. This plan outlines how to update the existing 50 issues without creating duplicates.

## User Review Required

> [!IMPORTANT]
> This plan assumes that the issue numbers 1-50 correspond to the order they were created in the original script.
> - **Milestones**: Issues will be assigned to specific milestones corresponding to their phase (e.g., "Phase 1: Rules & Models").
> - **Labels**: Each issue will be assigned a `phase:*` label based on its group.

## Proposed Changes

### GitHub Issue Updates

I will run a series of `gh issue edit` commands to update the issues.

#### [MODIFY] [update_issues.sh](file:///Users/donaldisoe/AndroidStudioProjects/SlideSlide/update_issues.sh) [NEW]
A temporary script to perform the updates.

```bash
#!/usr/bin/env bash

REPO="donald-okara/slide-slide"

echo "Updating Epic..."
gh issue edit 1 --repo "$REPO" --milestone "MVP" --add-label "phase:rules-models"

echo "Updating Phase 1..."
gh issue edit {2..9} --repo "$REPO" --milestone "Phase 1: Rules & Models" --add-label "phase:rules-models"

echo "Updating Phase 2..."
gh issue edit {10..16} --repo "$REPO" --milestone "Phase 2: Room Data Source" --add-label "phase:datasource"

echo "Updating Phase 3..."
gh issue edit {17..29} --repo "$REPO" --milestone "Phase 3: Puzzle Manager" --add-label "phase:domain"

echo "Updating Phase 4..."
gh issue edit {30..37} --repo "$REPO" --milestone "Phase 4: ViewModel" --add-label "phase:viewmodel"

echo "Updating Phase 5..."
gh issue edit {38..50} --repo "$REPO" --milestone "Phase 5: Compose UI" --add-label "phase:ui"

echo "✅ All issues updated successfully."
```

## Verification Plan

### Manual Verification
- Run `gh issue list --repo donald-okara/slide-slide --json number,title,labels,milestone` to verify the updates.
- Check a few random issues on GitHub UI to ensure they have the correct labels and are part of the "MVP" milestone.
