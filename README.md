# noble. The norcane blog engine.
*Norcane blog engine (noble)* is the blog engine written **by developers for developers**. It's
built using the [Play! Framework](https://www.playframework.com), so it can be seamlessly embedded
into your existing *Play!* applications. But the main idea is to **keep it simple**. No obscure ways
of storing blog posts, no annoying embedded *WYSIWYG* editors for more bizarre blog post format.
*Noble* by default uses as much tools you probably use every day: [Git](https://git-scm.com) as a
blog posts and assets storage, [Markdown](http://daringfireball.net/projects/markdown/) for blog
posts format and all *Play!*'s goodies for writing themes, such as
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
- [x] Can be easily embedded into any existing *Play!* using few simple steps (see chapter
  *Embedding in your Play! application* below).
- [x] Modular blog storage support. Default bundled implementation uses *Git* with directory
  structure similar to [Jekyll](https://jekyllrb.com).
- [x] Modular blog post format support, your blog posts can use in **multiple blog post formats**
  within a single blog. Default bundled implementation uses *Markdown* with *YAML*
  front matter, similar to [Jekyll](https://jekyllrb.com).
- [x] Completely themeable, with default theme *Humane* bundled.
- [x] Single installation can host one or multiple blogs.
- [x] Built-in, zero-conf support for *Atom* feeds.

## Planned features
- [ ] Support for static pages.
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

## Embedding in your Play! application
Embedding *Noble* into an existing *Play!* application should be pretty easy and straightforward
using the following steps. This tutorial expects you to use the default *Git* blog post storage and
*Markdown* blog post format support.

1. **Add the *Noble* dependency into your *Play!* SBT project**
   
   > This step cannot be performed at this moment, because *Noble* has not been released yet. We're
     working hard to publish it as soon as possible. Sorry for inconvenience.
     
2. **Add a route to *Noble* router into your *routes* file**

   ```
   ->  /   com.norcane.noble.NobleRouter
   ```
   
3. **Configure *Noble* in your *application.conf* file**

   *Noble* reads the configuration under the `com.norcane.noble` prefix. As an example, the
   configuration of *Minimal* blog is used below. *Noble* tries its best to describe you where the
   problem is if you forget to add any of the required fields.
   
   ```
   com.norcane.noble {
   
     ## Available blogs configuration.
     blogs {
   
       ## Configuration of example blog, called 'minimal'. This blog name is just internal, so it can
       ## be anything you want.
       minimal {
   
         ## The path of the blog, should be empty for root path and should not end with trailing
         ## slash. This path will be relative to the path that the blogs router is set to using the
         ## 'routes' file.
         path = ""
   
         ## The reload token is used to authenticate the blog reload requests. If not defined, blog
         ## reloading functionality is disabled.
         reloadToken = "4a264cab-321f-4e87-8bf3-4358d9c0dd9f"
   
         ## This is the configuration of blog files storage, noble application is designed to support
         ## multiple different storages in pluggable way.
         storage {
   
           ## This example blog uses Git repository as its file storage.
           type = "git"
   
           ## This is the configuration of the selected storage type. For different storage types, the
           ## content of this configuration may vary.
           config {
   
             ## Path to the Git repository. The repository must be already cloned and available on the
             ## filesystem.
             repoPath = "."
   
             ## (Optional) The path within the Git repository where to serve the blog from. If not
             ## specified, Git repository root will be used.
             blogPath = "examples/minimal/blog/"
   
             ## (Optional) The Git branch to read the blog from. If not specified, 'master' branch
             ## will be used.
             branch = "master"
             
             ## (Optional) The name of the remote to fetch from. If not specified, no fetch will be
             ## done during updating.
             remote = "origin"
           }
         }
       }
     }
   }
   ```
   
4. **Prepare the *Git* repository I. - Git structure overview**

   > In the previous step, we defined that our blog files will be loaded from the directory
     `examples/minimal/blog` withing the *Git* repo. Let's call this directory the
     *blog root directory* in the rest of this tutorial.
   
   The default implementation of *blog storage* uses the *Git* as a actual blog posts and assets
   storage. Bellow is the overview of possible structure, individual parts are explained in next
   chapters.
   
   ```
   examples/minimal/blog/
   ├── _assets                            ## directory that contains all blog assets
   │   └── images
   │       ├── avatars
   │       │   └── john-smith.jpg
   │       └── bluebox.png
   ├── _config.yml                        ## main blog configuration file
   └── _posts                             ## directory that contains all blog posts
       └── 2016-07-27-test-blog-post.md
   ```

5. **Prepare the *Git* repository II. - blog configuration file**
   
   Similarly as you configured the *Noble* in *application.conf* file, now the blog *Minimal* needs
   to be configured. This is achieved using the *_config.yml* file placed in the
   *blog root directory*. This file contains both required fields and possibly also fields specific
   for selected *blog theme*. Bellow is the example used in the *Minimal* blog:
   
   ```yaml
   ## Title of the blog
   title: Minimal test blog
   
   ## Subtitle of the blog (optional)
   subtitle: based on the Humane theme
   
   ## Name of used blog theme. This uses the default bundled theme called 'humane'
   theme: humane
   
   ## Blog copyright info (optional)
   copyright: Copyright © 2016 John Smith
   
   ## Blog authors (at least one author per blog must be defined)
   authors:
   
     ## Author with unique nickname 'john.smith'
     john.smith:
   
       ## Real name of the author
       name: John Smith
   
       ## Path to the author avatar/portrait image (optional, used by the Humane theme)
       avatar: images/avatars/john-smith.jpg
   
       ## Short biography of the author (optional, used by the Humane theme)
       biography: >
         Here belongs short author biography. Some themes may provide support for some formatting
         options, for example the *Humane* theme allows to format biography using the *Markdown*
         format.
   ```

6. **Prepare the *Git* repository III. - add example blog post**

   Below is the example of valid blog post, a simplified form of the one tha can be found in
   *Minimal* blog. *Noble* provides modular support for blog post format, the default bundled one
   uses the *Markdown* syntax with *YAML* front matter. Note that in single blog, you can use blog
   posts with multiple different formats, *Noble* always pick up the proper post format
   implementation for the particular blog post (when using the default *Git* based
   *blog post storage*, proper format is always picked based on the blog post file extension).
   
   ```markdown
   ---
   title: Test blog post title     # Blog post title
   author: john.smith              # Must be the nickname of author defined in _config.yml
   date: 2016-07-27                # Optional. If not specified, taken from file name.
   tags: noble blog norcane        # Use + instead of spaces for multi word tags (e.g. foo+bar)
   ---
  
   Hello world!
   
   Inline image below:
   
   ![blue box image](@@assets@@/images/bluebox.png)
   
   ```
   
   > Note the `@@assets@@` placeholder in the asset path above. You should **always** use this one
     to link the asset file, as *Noble* properly replaces this placeholder with proper URI when
     rendering the blog post content.

7. **Commit the changes in *Git* repo and start your application**

   Now *Noble* should be fully embedded into your application. When you start your *Play!*
   application, it should be accessible at [http://localhost:9000](http://localhost:9000). You can
   change the context root of the entire blog by changing the route in your *routes* file, like the
   following:
   
   ```
   ->  /myBlog   com.norcane.noble.NobleRouter
   ```
   
   The blog will now be accessible at [http://localhost:9000/myBlog](http://localhost:9000/myBlog).
   
   ---
   
   > This documentation is currently not complete. More chapters about writting own storage supports,
     format supports and themes coming soon!
