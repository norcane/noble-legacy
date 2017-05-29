# noble. The norcane blog engine.
*Norcane blog engine (noble)* is the blog engine written **by developers for developers**. It's
built using the [Play Framework](https://www.playframework.com), so it can be seamlessly embedded
into your existing *Play* applications. But the main idea is to **keep it simple**. No obscure ways
of storing blog posts, no annoying embedded *WYSIWYG* editors for more bizarre blog post format.
*Noble* by default uses as much tools you probably use every day: [Git](https://git-scm.com) as a
blog posts and assets storage, [Markdown](http://daringfireball.net/projects/markdown/) for blog
posts format and all *Play*'s goodies for writing themes, such as
[Twirl](https://www.playframework.com/documentation/2.5.x/ScalaTemplates) templates,
[Sass](http://sass-lang.com) or [Less](http://lesscss.org) for styling,
[Scala.js](https://www.scala-js.org) for scripting and much more! Don't like this selection? Never
mind, because **noble is modular**! Wanna *blog storage* that uses database? Or *blog post format*
that uses the *Wiki* syntax? No problem, just write your own.

> Please note that despite our best efforts, *noble* was primarily written for our internal purposes
and is still under heavy development. It means it's NOT feature complete yet (see the
*Planned features* chapter below) and its API may change until it reaches the *1.0.0* version. It is
also not yet released into the *Maven* repository, although this is planned to be done soon.

## Key features
- [x] Can be easily embedded into any existing *Play* using few simple steps (see chapter
  *Embedding in your Play application* below).
- [x] Modular blog storage support. Default bundled implementation uses *Git* with directory
  structure similar to [Jekyll](https://jekyllrb.com).
- [x] Modular blog post format support, your blog posts can use in **multiple blog post formats**
  within a single blog. Default bundled implementation uses *Markdown* with *YAML*
  front matter, similar to [Jekyll](https://jekyllrb.com).
- [x] Completely themeable, with default theme *Humane* bundled.
- [x] Support for static pages.
- [x] Single installation can host one or multiple blogs.
- [x] Built-in, zero-conf support for *Atom* feeds.

## Planned features
- [ ] *XML*/*JSON* API for better integration with client-side code.

## Try it out
*Noble* source code distribution contains the example blog called *Minimal*, which you can easily
try out using the following steps:

1. **Clone the *noble* repository**  
   `git clone https://github.com/norcane/noble.git`
2. **Go to the directory and run the *Minimal* example blog**  
   Note that this step requires the [Scala Build Tool (SBT)](http://www.scala-sbt.org) installed.

   ```
   $ cd noble
   $ sbt minimal/run
   ```

   At this moment, *Example* blog should be accessible by your browser at
   [http://localhost:9000](http://localhost:9000).

## Documentation
The [noble wiki](https://github.com/norcane/noble/wiki) serves as the main project documentation and
covers all steps needed to successfully embed *noble* into your *Play* application and write your
custom themes.

## Maintainers
Below is the list of current project maintainers. Feel free to contact us in case of any troubles.

* Václav Švejcar - [#vaclavsvejcar](https://github.com/vaclavsvejcar)
* Ján Naď - [#jannad](https://github.com/jannad)

## License
This project is licensed under the terms of the
[Apache License, version 2.0](https://www.apache.org/licenses/LICENSE-2.0).