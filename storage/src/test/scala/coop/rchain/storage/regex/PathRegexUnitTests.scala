package coop.rchain.storage.regex
import org.scalatest._
import scala.language.implicitConversions

class PathRegexUnitTests extends FlatSpec with Matchers {

  class ExPathRegex(value: PathRegex) {
    def accept(matchCases: (String, Seq[String])*): ExPathRegex = {
      value.Regex match {
        case Right(regex) =>
          for ((sample, sampleGroups) <- matchCases) {
            val matches = regex.findAllMatchIn(sample).toList
            //we expect only one match in source path, otherwise something is wrong
            assert(matches.length <= 1)
            if (matches.nonEmpty) {
              val foundMatch   = matches.head
              val plainMatched = foundMatch.matched :: foundMatch.subgroups

              assert(plainMatched == sampleGroups,
                     s"::: $sample => $sampleGroups != ${foundMatch.subgroups}")
            } else {
              assert(Nil == sampleGroups, s"::: $sample => $sampleGroups != Nil")
            }
          }
        case Left(err) =>
          if (matchCases.toSeq.nonEmpty) {
            fail(err)
          }
      }
      this
    }

    def build(samples: (Map[String, Iterable[String]], Either[Throwable, String])*): ExPathRegex = {
      for ((args, expectedResult) <- samples) {
        (value.toPath(args),expectedResult) match {
          case (Right(path), Right(expectedPath)) =>
            assert(expectedPath.contains(path))
          case (Right(path), Left(expectedErr)) =>
            fail(s"Expected error instead of path: '$path'")
          case (Left(err), Left(expectedErr)) =>
            assert(err.getClass == expectedErr.getClass)
          case (Left(err), Right(expectedPath)) =>
            fail(s"Expected Path: '$expectedPath' instead of error: '$err'")
        }
      }
      this
    }

    def struct(testTokens: List[PathToken]): ExPathRegex = {
      assert(testTokens == value.tokens)
      this
    }
  }

  def parse(path: String, options: PathRegexOptions = PathRegexOptions.default): ExPathRegex =
    new ExPathRegex(PathRegex(path, options))

  "Empty paths" should "parse" in {
    parse("/")
      .struct(List(PathToken("/")))
      .accept(("/", List("/")), ("/route", Nil))
      .build((Map(), Right("/")), (Map("id" -> List("123")), Right("/")))
  }

  "Simple paths" should "parse" in {
    parse("/test")
      .struct(List(PathToken("/test")))
      .accept(("/test", List("/test")),
              ("/route", Nil),
              ("/test/route", Nil),
              ("/test/", List("/test/")))
      .build((Map(), Right("/test")), (Map("id" -> List("123")), Right("/test")))

    parse("/test/")
      .struct(List(PathToken("/test/")))
      .accept(("/test", Nil), ("/test/", List("/test/")), ("/test//", List("/test//")))
      .build((Map(), Right("/test/")))
  }

  "Simple path" should "support escaped groups" in {
    parse("/te\\~st", PathRegexOptions.caseSensitive)
      .struct(List(PathToken("/te~st")))
      .accept(("/te~st", List("/te~st")), ("/TE~ST", Nil))
      .build((Map(), Right("/te~st")))
  }

  "Case-sensitive" should "work" in {
    parse("/test", PathRegexOptions.caseSensitive)
      .struct(List(PathToken("/test")))
      .accept(("/test", List("/test")), ("/TEST", Nil))
      .build((Map(), Right("/test")))

    parse("/TEST", PathRegexOptions.caseSensitive)
      .struct(List(PathToken("/TEST")))
      .accept(("/test", Nil), ("/TEST", List("/TEST")))
      .build((Map(), Right("/TEST")))
  }

  "Strict mode" should "work" in {
    parse("/test", PathRegexOptions.strict)
      .struct(List(PathToken("/test")))
      .accept(("/test", List("/test")), ("/test/", Nil), ("/TEST", List("/TEST")))
      .build((Map(), Right("/test")))

    parse("/test/", PathRegexOptions.strict)
      .struct(List(PathToken("/test/")))
      .accept(("/test", Nil), ("/test/", List("/test/")), ("/test//", Nil))
      .build((Map(), Right("/test/")))
  }

  "Non-ending mode" should "work" in {
    parse("/test", PathRegexOptions.nonEnd)
      .struct(List(PathToken("/test")))
      .accept(("/test/route", List("/test")),
              ("/test", List("/test")),
              ("/test/", List("/test/")),
              ("/route", Nil))
      .build((Map(), Right("/test")))

    parse("/test/", PathRegexOptions.nonEnd)
      .struct(List(PathToken("/test/")))
      .accept(("/test/route", List("/test/")),
              ("/test//route", List("/test/")),
              ("/test", Nil),
              ("/test//", List("/test//")))
      .build((Map(), Right("/test/")))

    parse("/:test", PathRegexOptions.nonEnd)
      .struct(
        List(PathToken(Some("test"), 0, Some('/'), Some('/'), false, false, false, """[^\/]+?""")))
      .accept(("/route", List("/route", "route")))
      .build(
            (Map(), Left(new IllegalArgumentException)),
            (Map("test" -> List("a+b")), Right("/a%2Bb")),
             (Map("test" -> List("abc")), Right("/abc")))

    parse("/:test/", PathRegexOptions.nonEnd)
      .struct(
        List(PathToken(Some("test"), 0, Some('/'), Some('/'), false, false, false, """[^\/]+?"""),
             PathToken("/")))
      .accept(("/route", Nil), ("/route/", List("/route/", "route")))
      .build((Map(), Left(new IllegalArgumentException)), (Map("test" -> List("abc")), Right("/abc/")))
  }

  val noEndStrictOptions = PathRegexOptions(end = false, strict = true)

  "Combine modes" should "accept simple paths" in {
    parse("/test", noEndStrictOptions)
      .struct(List(PathToken("/test")))
      .accept(("/test", List("/test")), ("/test/", List("/test")), ("/test/route", List("/test")))
      .build((Map(), Right("/test")))

    parse("/test/", noEndStrictOptions)
      .struct(List(PathToken("/test/")))
      .accept(("/test", Nil),
              ("/test/", List("/test/")),
              ("/test//", List("/test/")),
              ("/test/route", List("/test/")))
      .build((Map(), Right("/test/")))
  }

  "Combine modes" should "accept file path" in {
    parse("/test.json", noEndStrictOptions)
      .struct(List(PathToken("/test.json")))
      .accept(("/test.json", List("/test.json")),
              ("/test.json.hbs", Nil),
              ("/test.json/route", List("/test.json")))
      .build((Map(), Right("/test.json")))
  }

  "Combine modes" should "work with named argument" in {
    parse("/:test", noEndStrictOptions)
      .struct(
        List(PathToken(Some("test"), 0, Some('/'), Some('/'), false, false, false, """[^\/]+?""")))
      .accept(("/route", List("/route", "route")), ("/route/", List("/route", "route")))
      .build((Map(), Left(new IllegalArgumentException)), (Map("test" -> List("abc")), Right("/abc")))

    parse("/:test/", noEndStrictOptions)
      .struct(
        List(PathToken(Some("test"), 0, Some('/'), Some('/'), false, false, false, """[^\/]+?"""),
             PathToken("/")))
      .accept(("/route", Nil), ("/route/", List("/route/", "route")))
      .build((Map(), Left(new IllegalArgumentException)), (Map("test" -> List("foobar")), Right("/foobar/")))
  }

  "Single named parameter" should "pass test set" in {
    parse("/:test")
      .struct(
        List(
          PathToken(Some("test"),
                    0,
                    Some('/'),
                    Some('/'),
                    optional = false,
                    repeat = false,
                    partial = false,
                    """[^\/]+?""")))
      .accept(
        ("/route", List("/route", "route")),
        ("/another", List("/another", "another")),
        ("/something/else", Nil),
        ("/route.json", List("/route.json", "route.json")),
        ("/something%2Felse", List("/something%2Felse", "something%2Felse")),
        ("/something%2Felse%2Fmore", List("/something%2Felse%2Fmore", "something%2Felse%2Fmore")),
        ("/;,:@&=+$-_.!~*()", List("/;,:@&=+$-_.!~*()", ";,:@&=+$-_.!~*()"))
      )
      .build(
        (Map("test" -> List("route")), Right("/route")),
        (Map("test" -> List("something/else")), Right("/something%2Felse")),
        (Map("test" -> List("something/else/more")), Right("/something%2Felse%2Fmore"))
      )
  }

  "Named parameter" should "support strict mode" in {
    parse("/:test", PathRegexOptions.strict)
      .struct(
        List(
          PathToken(Some("test"),
                    0,
                    Some('/'),
                    Some('/'),
                    optional = false,
                    repeat = false,
                    partial = false,
                    """[^\/]+?""")))
      .accept(("/route", List("/route", "route")), ("/route/", Nil))
      .build((Map("test" -> List("route")), Right("/route")))

    parse("/:test/", PathRegexOptions.strict)
      .struct(
        List(PathToken(Some("test"),
                       0,
                       Some('/'),
                       Some('/'),
                       optional = false,
                       repeat = false,
                       partial = false,
                       """[^\/]+?"""),
             PathToken("/")))
      .accept(("/route/", List("/route/", "route")), ("/route//", Nil))
      .build((Map("test" -> List("route")), Right("/route/")))
  }

  "Named parameter" should "support non-ending mode" in {
    parse("/:test", PathRegexOptions.nonEnd)
      .struct(
        List(
          PathToken(Some("test"),
                    0,
                    Some('/'),
                    Some('/'),
                    optional = false,
                    repeat = false,
                    partial = false,
                    """[^\/]+?""")))
      .accept(("/route.json", List("/route.json", "route.json")),
              ("/route//", List("/route", "route")))
      .build((Map("test" -> List("route")), Right("/route")))
  }

  "Optional named parameter" should "work" in {
    parse("/:test?")
      .struct(
        List(
          PathToken(Some("test"),
                    0,
                    Some('/'),
                    Some('/'),
                    optional = true,
                    repeat = false,
                    partial = false,
                    """[^\/]+?""")))
      .accept(("/route", List("/route", "route")),
              ("/route/nested", Nil),
              ("/", List("/", null)),
              ("//", Nil))
  }

  "Optional named parameter" should "work with strict mode" in {
    parse("/:test?", PathRegexOptions.strict)
      .struct(
        List(
          PathToken(Some("test"),
                    0,
                    Some('/'),
                    Some('/'),
                    optional = true,
                    repeat = false,
                    partial = false,
                    """[^\/]+?""")))
      .accept(("/route", List("/route", "route")), ("/", Nil), ("//", Nil))
      .build((Map(), Right("")), (Map("test" -> List("foobar")), Right("/foobar")))

    parse("/:test?/", PathRegexOptions.strict)
      .struct(
        List(PathToken(Some("test"),
                       0,
                       Some('/'),
                       Some('/'),
                       optional = true,
                       repeat = false,
                       partial = false,
                       """[^\/]+?"""),
             PathToken("/")))
      .accept(("/route", Nil),
              ("/route/", List("/route/", "route")),
              ("/", List("/", null)),
              ("//", Nil))
      .build((Map(), Right("/")), (Map("test" -> List("foobar")), Right("/foobar/")))
  }

  "Optional named parameter" should "work well in the middle of path" in {
    parse("/:test?/bar")
      .struct(
        List(PathToken(Some("test"),
                       0,
                       Some('/'),
                       Some('/'),
                       optional = true,
                       repeat = false,
                       partial = false,
                       """[^\/]+?"""),
             PathToken("/bar")))
      .accept(("/foo/bar", List("/foo/bar", "foo")))
      .build((Map("test" -> List("foo")), Right("/foo/bar")))

    parse("/:test?-bar")
      .struct(
        List(PathToken(Some("test"),
                       0,
                       Some('/'),
                       Some('/'),
                       optional = true,
                       repeat = false,
                       partial = true,
                       """[^\/]+?"""),
             PathToken("-bar")))
      .accept(("/-bar", List("/-bar", null)), ("/foo-bar", List("/foo-bar", "foo")))
      .build((Map("test" -> List("aaa")), Right("/aaa-bar")))

    parse("/:test*-bar")
      .struct(
        List(PathToken(Some("test"),
                       0,
                       Some('/'),
                       Some('/'),
                       optional = true,
                       repeat = true,
                       partial = true,
                       """[^\/]+?"""),
             PathToken("-bar")))
      .accept(("/-bar", List("/-bar", null)),
              ("/foo-bar", List("/foo-bar", "foo")),
              ("/foo/baz-bar", List("/foo/baz-bar", "foo/baz")))
      .build((Map("test" -> List("aaa")), Right("/aaa-bar")),
             (Map("test" -> List("aaa", "bbb")), Right("/aaa-bbb-bar")))
  }

  "Repeated one or more times parameters." should "work" in {
    parse("/:test+")
      .struct(
        List(
          PathToken(Some("test"),
                    0,
                    Some('/'),
                    Some('/'),
                    optional = false,
                    repeat = true,
                    partial = false,
                    """[^\/]+?""")))
      .accept(("/", Nil),
              ("//", Nil),
              ("/route", List("/route", "route")),
              ("/some/basic/route", List("/some/basic/route", "some/basic/route")))
      .build((Map(), Left(new IllegalArgumentException)),
             (Map("test" -> List("foobar")), Right("foobar")),
             (Map("test" -> List("a", "b", "c")), Right("/a/b/c")))
  }

  "Repeated parameter" should "support inline regex" in {
    parse("/:test(\\d+)+")
      .struct(
        List(
          PathToken(Some("test"),
                    0,
                    Some('/'),
                    Some('/'),
                    optional = false,
                    repeat = true,
                    partial = false,
                    """\d+""")))
      .accept(("/abc/456/789", Nil), ("/123/456/789", List("/123/456/789", "123/456/789")))
      .build((Map("test" -> List("abc")), Left(new IllegalArgumentException)),
             (Map("test" -> List("123")), Right("/123")),
             (Map("test" -> List("1", "2", "3")), Right("/1/2/3")))

    parse("/route.:ext(json|xml)+")
      .struct(
        List(PathToken("/route"),
             PathToken(Some("ext"),
                       0,
                       Some('.'),
                       Some('.'),
                       optional = false,
                       repeat = true,
                       partial = false,
                       """json|xml""")))
      .accept(("/route", Nil),
              ("/route.json", List("/route.json", "json")),
              ("/route.xml.json", List("/route.xml.json", "xml.json")),
              ("/route.html", Nil))
      .build((Map("ext" -> List("foobar")), Left(new IllegalArgumentException)),
             (Map("ext" -> List("xml")), Right("/route.xml")),
             (Map("ext" -> List("xml", "json")), Right("/route.xml.json")))
  }

  "Repeated zero or more times parameters" should "be supported" in {
    parse("/:test*")
      .struct(
        List(
          PathToken(Some("test"),
                    0,
                    Some('/'),
                    Some('/'),
                    optional = true,
                    repeat = true,
                    partial = false,
                    """[^\/]+?""")))
      .accept(("/", List("/", null)),
              ("//", Nil),
              ("/route", List("/route", "route")),
              ("/some/basic/route", List("/some/basic/route", "some/basic/route")))
      .build((Map(), Left(new IllegalArgumentException)),
             (Map("test" -> List("foobar")), Right("/foobar")),
             (Map("test" -> List("foo", "bar")), Right("/foo/bar")))

    parse("/route.:ext([a-z]+)*")
      .struct(
        List(PathToken("/route"),
             PathToken(Some("ext"),
                       0,
                       Some('.'),
                       Some('.'),
                       optional = true,
                       repeat = true,
                       partial = false,
                       """[a-z]+""")))
      .accept(("/route", List("/route", null)),
              ("/route.json", List("/route.json", "json")),
              ("/route.json.xml", List("/route.json.xml", "json.xml")),
              ("/route.123", Nil))
      .build(
        (Map(), Right("/route")),
        (Map("ext" -> Nil), Right("/route")),
        (Map("ext" -> List("123")), Left(new IllegalArgumentException)),
        (Map("ext" -> List("foobar")), Right("/route.foobar")),
        (Map("ext" -> List("foo", "bar")), Right("/foute.foo.bar"))
      )
  }

  "Custom named parameters (no repeat)" should "work" in {
    parse("/:test(\\d+)")
      .struct(
        List(
          PathToken(Some("test"),
                    0,
                    Some('/'),
                    Some('/'),
                    optional = false,
                    repeat = false,
                    partial = false,
                    """\d+""")))
      .accept(("/abc", Nil), ("/123", List("/123", "123")))
      .build((Map("test" -> List("abc")), Left(new IllegalArgumentException)),
        (Map("test" -> List("123")), Right("/123")))

    parse("/:test(\\d+)", PathRegexOptions.nonEnd)
      .struct(
        List(
          PathToken(Some("test"),
                    0,
                    Some('/'),
                    Some('/'),
                    optional = false,
                    repeat = false,
                    partial = false,
                    """\d+""")))
      .accept(("/abc", Nil), ("/123", List("/123", "123")), ("/123/abc", List("/123", "123")))
      .build((Map("test" -> List("abc")), Left(new IllegalArgumentException)),
        (Map("test" -> List("123")), Right("/123")))
  }

  "Custom named parameter" should "support wildcard" in {
    parse("/:test(.*)")
      .struct(
        List(
          PathToken(Some("test"),
                    0,
                    Some('/'),
                    Some('/'),
                    optional = false,
                    repeat = false,
                    partial = false,
                    """.*""")))
      .accept(("/anything/goes/here", List("/anything/goes/here", "anything/goes/here")),
              ("/;,:@&=/+$-_.!/~*()", List("/;,:@&=/+$-_.!/~*()", ";,:@&=/+$-_.!/~*()")))
      .build(
        (Map("test" -> List("")), Right("/")),
        (Map("test" -> List("abc")), Right("/abc")),
        (Map("test" -> List("abc/123")), Right("/abc%2F123")),
        (Map("test" -> List("abc/123/456")), Right("/abc%2F123%2F456"))
      )
  }
}
