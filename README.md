# github.clj.alpha


``` shell
clojure -Sdeps '{:deps {ajchemist/github.clj.alpha {:git/url "https://github.com/ajchemist/github.clj.alpha" :sha "f96dac478fce55e655608da9c3816b4f7e7cfda0"}}}' \
    -- \
    -m github.core.alpha.script.repo-actions \
    list-secrets \
    --owner OWNER \
    --repo REPO \
    --basic-auth BASIC_AUTH
```
