# github.clj.alpha


[![clojars badge](https://img.shields.io/clojars/v/ajchemist/github.clj.alpha.svg?style=flat-square)](https://clojars.org/ajchemist/github.clj.alpha)


# Tools


``` shell
clojure -Ttools install-latest :lib io.github.ajchemist/github.clj.alpha :as github
clojure -Tgithub setup-secrets-from-pass :basic-auth ""\"user:$(pass user-secret)\""" :repository owner/repo
```


# CLI


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
