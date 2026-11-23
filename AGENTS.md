# Marie fork rules

Rules for this fork (`devuterian/marie-PebbleApp`). Upstream's guidance stays in
`CLAUDE.md`.

## Git rules

- `origin` keeps only the default branch (`master`). Versions live in tags and
  GitHub releases; do not push release, backup or merge branches.
- Work on short-lived local branches and delete them, locally and on `origin`,
  as soon as they are merged. GitHub deletes merged pull request branches
  automatically.
- Sync upstream on a local `sync/upstream-YYYYMMDD` branch, merge it into
  `master`, then delete it. If something needs keeping, use an annotated
  `archive/<name>` tag instead of a branch.
- Do not mirror upstream branches or pull requests into `origin`. Read them
  from `upstream`; to port one, `git fetch upstream pull/N/head:port/prN`, work
  there and delete it after merging.
- Before deleting a branch that has commits not in `master`, tag it
  `archive/<branch>` and push the tag.
- Remove agent worktrees and their branches when the task ends.
