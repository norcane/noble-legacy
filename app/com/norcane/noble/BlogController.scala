package com.norcane.noble

import play.api.mvc.{Action, Results}

class BlogController extends Results {

  def test = Action {
    Ok("Hello, world")
  }
}
