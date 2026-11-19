# Release

The agent does all of this except the push, and only when the maintainer asks. The
`v-herd-demo` branch always reflects the released version, because the demo server
deploys from it. The steps must happen in this order so the tag, the demo branch and
the next snapshot line up. This applies to every release (patch, minor or major).

1. Strip `-SNAPSHOT` from every `<version>` and `<fullcalendar.version>` in all poms
   (root, `addon`, `addon-scheduler`, `demo`, `e2e-test-app`).
2. Set `ADDON_VERSION` in the demo's `AbstractLayout` to the release version, without
   `-SNAPSHOT`. The demo footer shows it.
3. Commit the pom and `ADDON_VERSION` changes together as `Release <version>`, so
   the tag and the `v-herd-demo` merge carry a consistent tree.
4. Tag the commit: `git tag -a <version> -m "Release <version>"`.
5. Don't bump the snapshot yet.
6. Check out `v-herd-demo` and merge master with `git merge master -X theirs`, which
   takes master's pom values over the historical `v-herd-version` commit. The tree
   must show the released version in all poms and in `ADDON_VERSION`, so
   `git diff <version> HEAD` comes back empty. Prepare the merge but don't push it.
7. The maintainer pushes master, the tag and `v-herd-demo` together and runs the
   release build. Pushing them in one go avoids a ping-pong between the branches.
   Never push a tag without the maintainer's confirmation, because tags are public
   the moment they reach the remote and can't be rewritten cleanly.
8. Back on master, bump to the next snapshot (usually the next patch, e.g.
   `7.2.1 → 7.2.2-SNAPSHOT`) in the poms and in `ADDON_VERSION`. Commit as
   `Bump to <next>-SNAPSHOT`.

## Why the order matters

If the snapshot is bumped before `v-herd-demo` is synced and pushed, merging master
into `v-herd-demo` brings in the snapshot version. The demo server then redeploys
against a `-SNAPSHOT` artifact that is in no public repository, and the deployment
breaks.

## Why `ADDON_VERSION` matters

The demo server renders it as "Version X". If it drifts from the pom version, the
deployed demo shows a misleading version. Forgetting it has historically required a
follow-up cherry-pick into `v-herd-demo`, which this order is meant to prevent.
