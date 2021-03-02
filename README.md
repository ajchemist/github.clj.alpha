# github.clj.alpha


[![clojars badge](https://img.shields.io/clojars/v/ajchemist/github.clj.alpha.svg?style=flat-square)](https://clojars.org/ajchemist/github.clj.alpha)
[![travis](https://img.shields.io/travis/ajchemist/github.clj.alpha/master.svg?style=flat-square)](https://travis-ci.com/ajchemist/github.clj.alpha)


``` shell
clojure -Sdeps '{:deps {ajchemist/github.clj.alpha {:git/url "https://github.com/ajchemist/github.clj.alpha" :sha "f96dac478fce55e655608da9c3816b4f7e7cfda0"}}}' \
    -- \
    -m github.core.alpha.script.repo-actions \
    list-secrets \
    --owner OWNER \
    --repo REPO \
    --basic-auth BASIC_AUTH
```


<!-- footer -->
