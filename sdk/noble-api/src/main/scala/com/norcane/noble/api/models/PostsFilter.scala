package com.norcane.noble.api.models

/**
  * Represents the filter which is set for the blog posts listing (e.g. only blog posts for year
  * 2016 are currently displayed). Provides possibility for custom themes to implement specific
  * look/behaviour for different filters.
  */
sealed trait PostsFilter

/**
  * Companion object, provides concrete implementations.
  */
object PostsFilter {

  /**
    * States that blog posts are filtered by concrete author.
    *
    * @param author author details
    */
  case class Author(author: BlogAuthor) extends PostsFilter

  /**
    * States that the blog posts are filtered by the year of publication.
    *
    * @param year year of publication
    */
  case class Year(year: Int) extends PostsFilter

  /**
    * States that the blog posts are filtered by the year and month of publication.
    *
    * @param year  year of publication
    * @param month month of publication
    */
  case class Month(year: Int, month: Int) extends PostsFilter

  /**
    * States that the blog posts are filtered by the year, month and day of publication.
    *
    * @param year  year of publication
    * @param month month of publication
    * @param day   day of publication
    */
  case class Day(year: Int, month: Int, day: Int) extends PostsFilter

  /**
    * States that the blog posts are filtered by specific tag.
    *
    * @param tag tag name
    */
  case class Tag(tag: String) extends PostsFilter

}


