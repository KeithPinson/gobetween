package com.keithpinson.gobetweentest

/*
 * Copyright (c) 2015 Keith Pinson.
 *
 * Created: 8/22/2015
 */

import com.keithpinson.gobetween.Markdown._
import org.specs2.{Specification, ScalaCheck }
import org.scalacheck.{Arbitrary, Gen, Prop}


/**
 * Test the Markdown class using ScalaCheck and Specs2 as specified in the Markdown Requirements.
 *
 * @see [[com.keithpinson.gobetween.TermsAndConditions]]<br/>
 *
 * @author [[http://keithpinson.com Keith Pinson]]
 */
class MarkdownTest extends Specification { def is = sequential ^
  "Markdown's original purpose was to convert type written text to html markup" ^ br ^
  "For our purposes, Markdown's job (including the embedded html) is to convert text to the GoBetween Intermediate Structure" ^ br ^
  new MarkdownTerminologyTest
  new MarkdownSyntaxTest
}

class MarkdownSyntaxTest extends Specification with ScalaCheck with MarkdownTestHelpers { def is = s2"""
  Any sequence of characters is a valid markdown document $checkDocument

"""
  def checkDocument = prop( (s:String) => failure ).setGen(genLine)
}


class MarkdownTerminologyTest extends Specification with ScalaCheck with MarkdownTestHelpers { def is = s2"""
  The Markdown Requirements Document defines the following terms to mean:

  A character is a unicode code point $checkCharacter
  Any file encoding can be used, ie. "UTF-8" or "windows-1252", so the size of the code point depends on the file encoding being used

  A line is a sequence of zero or more characters followed by an end of line $checkLine

  An end of line is one of the following:
      An end of File
      A newline (U+000A)
      A carriage return (U+000D)
      Or, a carriage return + newline (U+000D,U+000A) $checkEOL

  A line containing no characters after all the whitespace has been removed is a blank line $checkBlankLine

  A space is U+0020 $checkSpace

  A tab is U+0009 $checkTab

  A period or full-stop is U+002E $checkDot

  A whitespace character is a character that moves the location of the cursor but does not print anything, it can be any of the following:
      &#x0009; ( ) 0x0009 "horizontal tab"
      &#x000B; () 0x000B "vertical tab"
      &#x000C; () 0x000C "form feed"
      &#x0020; () 0x0020 "space"
      &nbsp;  () 0x00A0 "no-break space"
      &#x1680; () 0x1680 "ogham space mark"
      &#x2000; () 0x2000 "en quad"
      &#x2001; () 0x2001 "em quad"
      &ensp; () 0x2002 "en space"
      &emsp; () 0x2003 "em space"
      &#x2004; () 0x2004 "three-per-em space"
      &#x2005; () 0x2005 "four-per-em space"
      &#x2006; () 0x2006 "six-per-em-space"
      &#x2007; () 0x2007 "figure space"
      &#x2008; () 0x2008 "punctuation space"
      &thinsp; () 0x2009 "thin space"
      &#x200A; () 0x200A "hair space"
      &#x202F; () 0x202F "narrow no-break space"
      &#x205F; () 0x205F "medium mathematical space"
      &#x3000; () 0x3000 "ideographic space" $checkWhitespaceChar

  Whitespace is a sequence of one or more whitespace characters $checkWhitespace

  A non-whitespace character is any character that is not a whitespace character $checkNonWhitespace

  A punctuation character is any one of the ASCII characters:
      (!, ", #, $$, %, &, ', (, ), *, +, ,, -, ., /, :, ;, <, =, >, ?, @, [, \, ], ^, _, `, {, |, }, ~) $checkPunctuation

  Any punctuation character can be preceded by a backslash (\) which indicates it should be treated like a literal punctuation character and not a markdown element
    Escaped punctuation does not include punctuation beyond the ASCII range $checkEscapedPunctuation
"""

  def checkCharacter = prop( (c:Char) => c must beBetween(0x0000.toChar,0x26FF.toChar) ).setGen(genUnicode)

  def checkLine = prop( (s:String) => s must beMatching(""".*(\n|\r|\r\n)$""") ).setGen(genLine)

  def checkEOL = prop( (s:String) => s.lengthCompare(2) <= 0 && (s must beMatching("""(\n|\r|\r\n)$""")) ).setGen(genEOL)

  def checkBlankLine = prop( (s:String) => s must beMatching("""^([\p{Zs}|\u0009|\u000B|\u000C])*(\n|\r|\r\n)$""") ).setGen(genBlankLine)

  def checkSpace = prop( (c:Char) => c must beEqualTo(0x0020.toChar) ).setGen(genSpace)

  def checkTab = prop( (c:Char) => c must beEqualTo(0x0009.toChar) ).setGen(genTab)

  def checkDot = prop( (c:Char) => c must beEqualTo(0x002E.toChar) ).setGen(genDot)

  // Don't test this with the same list of whitespace characters that created the generator (that won't test anything), use a regular expression instead
  def checkWhitespaceChar = prop( (s:String) => s.lengthCompare(1) == 0 && (s must beMatching("""([\p{Zs}|\u0009|\u000B|\u000C])""")) ).setGen(genWhitespaceChar)

  def checkWhitespace = prop( (s:String) => s must beMatching("""([\p{Zs}|\u0009|\u000B|\u000C])*""") ).setGen(genWhitespace)

  def checkNonWhitespace = prop( (s:String) => s must beMatching("""(^[\p{Zs}|\u0009|\u000B|\u000C])*""") ).setGen(genNonWhitespace)

  def checkPunctuation = prop( (c:Char) => (c must beLessThan(0x7f.toChar)) && (c.toString must beMatching("""([\p{P}|\$|\+|\<|\=|\>|\^|\`|\||\~])""")) ).setGen(genPunctuationChar)

  def checkUnicodePunctuation = prop( (c:Char) => (c must beGreaterThan(0x7e.toChar)) && (c.toString must beMatching("""([\p{P}])""")) ).setGen(genUnicodePunctuationChar)

  def checkEscapedPunctuation = prop( (s:String) => s.lengthCompare(2) == 0 &&
    (s.head must beEqualTo('\\')) &&
    (s.last must beLessThan(0x7f.toChar)) &&
    (s.last.toString must beMatching("""([\p{P}|\$|\+|\<|\=|\>|\^|\`|\||\~])""")) ).setGen(genEscapedPunctuation)

}

trait MarkdownTestHelpers {
  def genUnicode : Gen[Char] = for { c <- Gen.chooseNum(0x0000,0x26FF) } yield c.toChar
  def genLine : Gen[String] = for { cc <- Gen.nonEmptyListOf(Gen.alphaNumChar); e <- genEOL } yield cc.take(80).mkString + e
  def genEOL : Gen[String] = for { cc <- Gen.oneOf("\n","\r","\r\n") } yield cc
  def genBlankLine : Gen[String] = for { ww <- genWhitespace; cc <- genEOL } yield ww + cc
  def genSpace : Gen[Char] = for { c <- Gen.const(' ') } yield c
  def genTab : Gen[Char] = for { c <- Gen.const('\t') } yield c
  def genDot : Gen[Char] = for { c <- Gen.const('.') } yield c
  def genWhitespaceChar : Gen[String] = for {w <- Gen.oneOf(ws)} yield w
  def genWhitespace : Gen[String] = for { ww <- Gen.listOf(genWhitespaceChar) } yield ww.take(60).mkString
  def genNonWhitespace : Gen[String] = for { ww <- Gen.listOf(genUnicode) } yield ww.filter( ws.contains(_) ).take(60).mkString
  def genPunctuationChar : Gen[Char] = for { c <- Gen.oneOf(punct)} yield c.head
  def genUnicodePunctuationChar : Gen[Char] = for { c <- Gen.oneOf(uPunct) } yield c.head
  def genEscapedPunctuation : Gen[String] = for { s <- genPunctuationChar } yield "\\" + s

  private val ws = List(
    "\u0009", // horizontal tab
    "\u000B", // vertical tab
    "\u000C", // form feed
    "\u0020", // space
    "\u00A0", // no-break space
    "\u1680", // ogham space mark
    "\u2000", // en quad
    "\u2001", // em quad
    "\u2002", // en space
    "\u2003", // em space
    "\u2004", // three-per-em space
    "\u2005", // four-per-em space
    "\u2006", // six-per-em-space
    "\u2007", // figure space
    "\u2008", // punctuation space
    "\u2009", // thin space
    "\u200A", // hair space
    "\u202F", // narrow no-break space
    "\u205F", // medium mathematical space
    "\u3000"  // ideographic space
  )

  private val punct = """!, ", #, $, %, &, ', (, ), *, +, ,, -, ., /, :, ;, <, =, >, ?, @, [, \, ], ^, _, `, {, |, }, ~""".split(", ")

  private val uPunct = List(
    // Connector Punctuation (Pc)
    "\u005F",	//LOW LINE
    "\u203F",	//UNDERTIE
    "\u2040",	//CHARACTER TIE
    "\u2054",	//INVERTED UNDERTIE
    "\uFE33",	//PRESENTATION FORM FOR VERTICAL LOW LINE
    "\uFE34",	//PRESENTATION FORM FOR VERTICAL WAVY LOW LINE
    "\uFE4D",	//DASHED LOW LINE
    "\uFE4E",	//CENTRELINE LOW LINE
    "\uFE4F",	//WAVY LOW LINE
    "\uFF3F",	//FULLWIDTH LOW LINE

    // Dash Punctuation (Pd)
    "\u002D",	//HYPHEN
    "\u058A",	//ARMENIAN HYPHEN
    "\u05BE",	//HEBREW PUNCTUATION MAQAF
    "\u1400",	//CANADIAN SYLLABICS HYPHEN
    "\u1806",	//MONGOLIAN T'ODO SOFT HYPHEN
    "\u2010",	//HYPHEN
    "\u2011",	//NON
    "\u2012",	//FIGURE DASH
    "\u2013",	//EN DASH
    "\u2014",	//EM DASH
    "\u2015",	//HORIZONTAL BAR
    "\u2E17",	//DOUBLE OBLIQUE HYPHEN
    "\u2E1A",	//HYPHEN WITH DIAERESIS
    "\u2E3A",	//TWO
    "\u2E3B",	//THREE
    "\u2E40",	//DOUBLE HYPHEN
    "\u301C",	//WAVE DASH
    "\u3030",	//WAVY DASH
    "\u30A0",	//KATAKANA
    "\uFE31",	//PRESENTATION FORM FOR VERTICAL EM DASH
    "\uFE32",	//PRESENTATION FORM FOR VERTICAL EN DASH
    "\uFE58",	//SMALL EM DASH
    "\uFE63",	//SMALL HYPHEN
    "\uFF0D",	//FULLWIDTH HYPHEN

    // Close Punctuation (Pe)
    "\u0029",	//RIGHT PARENTHESIS
    "\u005D",	//RIGHT SQUARE BRACKET
    "\u007D",	//RIGHT CURLY BRACKET
    "\u0F3B",	//TIBETAN MARK GUG RTAGS GYAS
    "\u0F3D",	//TIBETAN MARK ANG KHANG GYAS
    "\u169C",	//OGHAM REVERSED FEATHER MARK
    "\u2046",	//RIGHT SQUARE BRACKET WITH QUILL
    "\u207E",	//SUPERSCRIPT RIGHT PARENTHESIS
    "\u208E",	//SUBSCRIPT RIGHT PARENTHESIS
    "\u2309",	//RIGHT CEILING
    "\u230B",	//RIGHT FLOOR
    "\u232A",	//RIGHT
    "\u2769",	//MEDIUM RIGHT PARENTHESIS ORNAMENT
    "\u276B",	//MEDIUM FLATTENED RIGHT PARENTHESIS ORNAMENT
    "\u276D",	//MEDIUM RIGHT
    "\u276F",	//HEAVY RIGHT
    "\u2771",	//HEAVY RIGHT
    "\u2773",	//LIGHT RIGHT TORTOISE SHELL BRACKET ORNAMENT
    "\u2775",	//MEDIUM RIGHT CURLY BRACKET ORNAMENT
    "\u27C6",	//RIGHT S
    "\u27E7",	//MATHEMATICAL RIGHT WHITE SQUARE BRACKET
    "\u27E9",	//MATHEMATICAL RIGHT ANGLE BRACKET
    "\u27EB",	//MATHEMATICAL RIGHT DOUBLE ANGLE BRACKET
    "\u27ED",	//MATHEMATICAL RIGHT WHITE TORTOISE SHELL BRACKET
    "\u27EF",	//MATHEMATICAL RIGHT FLATTENED PARENTHESIS
    "\u2984",	//RIGHT WHITE CURLY BRACKET
    "\u2986",	//RIGHT WHITE PARENTHESIS
    "\u2988",	//Z NOTATION RIGHT IMAGE BRACKET
    "\u298A",	//Z NOTATION RIGHT BINDING BRACKET
    "\u298C",	//RIGHT SQUARE BRACKET WITH UNDERBAR
    "\u298E",	//RIGHT SQUARE BRACKET WITH TICK IN BOTTOM CORNER
    "\u2990",	//RIGHT SQUARE BRACKET WITH TICK IN TOP CORNER
    "\u2992",	//RIGHT ANGLE BRACKET WITH DOT
    "\u2994",	//RIGHT ARC GREATER
    "\u2996",	//DOUBLE RIGHT ARC LESS
    "\u2998",	//RIGHT BLACK TORTOISE SHELL BRACKET
    "\u29D9",	//RIGHT WIGGLY FENCE
    "\u29DB",	//RIGHT DOUBLE WIGGLY FENCE
    "\u29FD",	//RIGHT
    "\u2E23",	//TOP RIGHT HALF BRACKET
    "\u2E25",	//BOTTOM RIGHT HALF BRACKET
    "\u2E27",	//RIGHT SIDEWAYS U BRACKET
    "\u2E29",	//RIGHT DOUBLE PARENTHESIS
    "\u3009",	//RIGHT ANGLE BRACKET
    "\u300B",	//RIGHT DOUBLE ANGLE BRACKET
    "\u300D",	//RIGHT CORNER BRACKET
    "\u300F",	//RIGHT WHITE CORNER BRACKET
    "\u3011",	//RIGHT BLACK LENTICULAR BRACKET
    "\u3015",	//RIGHT TORTOISE SHELL BRACKET
    "\u3017",	//RIGHT WHITE LENTICULAR BRACKET
    "\u3019",	//RIGHT WHITE TORTOISE SHELL BRACKET
    "\u301B",	//RIGHT WHITE SQUARE BRACKET
    "\u301E",	//DOUBLE PRIME QUOTATION MARK
    "\u301F",	//LOW DOUBLE PRIME QUOTATION MARK
    "\uFD3E",	//ORNATE LEFT PARENTHESIS
    "\uFE18",	//PRESENTATION FORM FOR VERTICAL RIGHT WHITE LENTICULAR BRAKCET
    "\uFE36",	//PRESENTATION FORM FOR VERTICAL RIGHT PARENTHESIS
    "\uFE38",	//PRESENTATION FORM FOR VERTICAL RIGHT CURLY BRACKET
    "\uFE3A",	//PRESENTATION FORM FOR VERTICAL RIGHT TORTOISE SHELL BRACKET
    "\uFE3C",	//PRESENTATION FORM FOR VERTICAL RIGHT BLACK LENTICULAR BRACKET
    "\uFE3E",	//PRESENTATION FORM FOR VERTICAL RIGHT DOUBLE ANGLE BRACKET
    "\uFE40",	//PRESENTATION FORM FOR VERTICAL RIGHT ANGLE BRACKET
    "\uFE42",	//PRESENTATION FORM FOR VERTICAL RIGHT CORNER BRACKET
    "\uFE44",	//PRESENTATION FORM FOR VERTICAL RIGHT WHITE CORNER BRACKET
    "\uFE48",	//PRESENTATION FORM FOR VERTICAL RIGHT SQUARE BRACKET
    "\uFE5A",	//SMALL RIGHT PARENTHESIS
    "\uFE5C",	//SMALL RIGHT CURLY BRACKET
    "\uFE5E",	//SMALL RIGHT TORTOISE SHELL BRACKET
    "\uFF09",	//FULLWIDTH RIGHT PARENTHESIS
    "\uFF3D",	//FULLWIDTH RIGHT SQUARE BRACKET
    "\uFF5D",	//FULLWIDTH RIGHT CURLY BRACKET
    "\uFF60",	//FULLWIDTH RIGHT WHITE PARENTHESIS
    "\uFF63",	//HALFWIDTH RIGHT CORNER BRACKET

    // Final Punctuation (Pf)
    "\u00BB",	//RIGHT
    "\u2019",	//RIGHT SINGLE QUOTATION MARK
    "\u201D",	//RIGHT DOUBLE QUOTATION MARK
    "\u203A",	//SINGLE RIGHT
    "\u2E03",	//RIGHT SUBSTITUTION BRACKET
    "\u2E05",	//RIGHT DOTTED SUBSTITUTION BRACKET
    "\u2E0A",	//RIGHT TRANSPOSITION BRACKET
    "\u2E0D",	//RIGHT RAISED OMISSION BRACKET
    "\u2E1D",	//RIGHT LOW PARAPHRASE BRACKET
    "\u2E21",	//RIGHT VERTICAL BAR WITH QUILL

    // Initial Punctuation (Pi)
    "\u00AB",	//LEFT
    "\u2018",	//LEFT SINGLE QUOTATION MARK
    "\u201B",	//SINGLE HIGH
    "\u201C",	//LEFT DOUBLE QUOTATION MARK
    "\u201F",	//DOUBLE HIGH
    "\u2039",	//SINGLE LEFT
    "\u2E02",	//LEFT SUBSTITUTION BRACKET
    "\u2E04",	//LEFT DOTTED SUBSTITUTION BRACKET
    "\u2E09",	//LEFT TRANSPOSITION BRACKET
    "\u2E0C",	//LEFT RAISED OMISSION BRACKET
    "\u2E1C",	//LEFT LOW PARAPHRASE BRACKET
    "\u2E20",	//LEFT VERTICAL BAR WITH QUILL

    // Other Punctuation (Po)
    "\u0021",	//EXCLAMATION MARK
    """\u0022""",	//QUOTATION MARK
    "\u0023",	//NUMBER SIGN
    "\u0025",	//PERCENT SIGN
    "\u0026",	//AMPERSAND
    "\u0027",	//APOSTROPHE
    "\u002A",	//ASTERISK
    "\u002C",	//COMMA
    "\u002E",	//FULL STOP
    "\u002F",	//SOLIDUS
    "\u003A",	//COLON
    "\u003B",	//SEMICOLON
    "\u003F",	//QUESTION MARK
    "\u0040",	//COMMERCIAL AT
    """\u005C""",	//REVERSE SOLIDUS
    "\u00A1",	//INVERTED EXCLAMATION MARK
    "\u00A7",	//SECTION SIGN
    "\u00B6",	//PILCROW SIGN
    "\u00B7",	//MIDDLE DOT
    "\u00BF",	//INVERTED QUESTION MARK
    "\u037E",	//GREEK QUESTION MARK
    "\u0387",	//GREEK ANO TELEIA
    "\u055A",	//ARMENIAN APOSTROPHE
    "\u055B",	//ARMENIAN EMPHASIS MARK
    "\u055C",	//ARMENIAN EXCLAMATION MARK
    "\u055D",	//ARMENIAN COMMA
    "\u055E",	//ARMENIAN QUESTION MARK
    "\u055F",	//ARMENIAN ABBREVIATION MARK
    "\u0589",	//ARMENIAN FULL STOP
    "\u05C0",	//HEBREW PUNCTUATION PASEQ
    "\u05C3",	//HEBREW PUNCTUATION SOF PASUQ
    "\u05C6",	//HEBREW PUNCTUATION NUN HAFUKHA
    "\u05F3",	//HEBREW PUNCTUATION GERESH
    "\u05F4",	//HEBREW PUNCTUATION GERSHAYIM
    "\u0609",	//ARABIC
    "\u060A",	//ARABIC
    "\u060C",	//ARABIC COMMA
    "\u060D",	//ARABIC DATE SEPARATOR
    "\u061B",	//ARABIC SEMICOLON
    "\u061E",	//ARABIC TRIPLE DOT PUNCTUATION MARK
    "\u061F",	//ARABIC QUESTION MARK
    "\u066A",	//ARABIC PERCENT SIGN
    "\u066B",	//ARABIC DECIMAL SEPARATOR
    "\u066C",	//ARABIC THOUSANDS SEPARATOR
    "\u066D",	//ARABIC FIVE POINTED STAR
    "\u06D4",	//ARABIC FULL STOP
    "\u0700",	//SYRIAC END OF PARAGRAPH
    "\u0701",	//SYRIAC SUPRALINEAR FULL STOP
    "\u0702",	//SYRIAC SUBLINEAR FULL STOP
    "\u0703",	//SYRIAC SUPRALINEAR COLON
    "\u0704",	//SYRIAC SUBLINEAR COLON
    "\u0705",	//SYRIAC HORIZONTAL COLON
    "\u0706",	//SYRIAC COLON SKEWED LEFT
    "\u0707",	//SYRIAC COLON SKEWED RIGHT
    "\u0708",	//SYRIAC SUPRALINEAR COLON SKEWED LEFT
    "\u0709",	//SYRIAC SUBLINEAR COLON SKEWED RIGHT
    "\u070A",	//SYRIAC CONTRACTION
    "\u070B",	//SYRIAC HARKLEAN OBELUS
    "\u070C",	//SYRIAC HARKLEAN METOBELUS
    "\u070D",	//SYRIAC HARKLEAN ASTERISCUS
    "\u07F7",	//NKO SYMBOL GBAKURUNEN
    "\u07F8",	//NKO COMMA
    "\u07F9",	//NKO EXCLAMATION MARK
    "\u0830",	//SAMARITAN PUNCTUATION NEQUDAA
    "\u0831",	//SAMARITAN PUNCTUATION AFSAAQ
    "\u0832",	//SAMARITAN PUNCTUATION ANGED
    "\u0833",	//SAMARITAN PUNCTUATION BAU
    "\u0834",	//SAMARITAN PUNCTUATION ATMAAU
    "\u0835",	//SAMARITAN PUNCTUATION SHIYYAALAA
    "\u0836",	//SAMARITAN ABBREVIATION MARK
    "\u0837",	//SAMARITAN PUNCTUATION MELODIC QITSA
    "\u0838",	//SAMARITAN PUNCTUATION ZIQAA
    "\u0839",	//SAMARITAN PUNCTUATION QITSA
    "\u083A",	//SAMARITAN PUNCTUATION ZAEF
    "\u083B",	//SAMARITAN PUNCTUATION TURU
    "\u083C",	//SAMARITAN PUNCTUATION ARKAANU
    "\u083D",	//SAMARITAN PUNCTUATION SOF MASHFAAT
    "\u083E",	//SAMARITAN PUNCTUATION ANNAAU
    "\u085E",	//MANDAIC PUNCTUATION
    "\u0964",	//DEVANAGARI DANDA
    "\u0965",	//DEVANAGARI DOUBLE DANDA
    "\u0970",	//DEVANAGARI ABBREVIATION SIGN
    "\u0AF0",	//GUJARATI ABBREVIATION SIGN
    "\u0DF4",	//SINHALA PUNCTUATION KUNDDALIYA
    "\u0E4F",	//THAI CHARACTER FONGMAN
    "\u0E5A",	//THAI CHARACTER ANGKHANKHU
    "\u0E5B",	//THAI CHARACTER KHOMUT
    "\u0F04",	//TIBETAN MARK INITIAL YIG MGO MDUN MA
    "\u0F05",	//TIBETAN MARK CLOSING YIG MGO SGAB MA
    "\u0F06",	//TIBETAN MARK CARET YIG MGO PHUR SHAD MA
    "\u0F07",	//TIBETAN MARK YIG MGO TSHEG SHAD MA
    "\u0F08",	//TIBETAN MARK SBRUL SHAD
    "\u0F09",	//TIBETAN MARK BSKUR YIG MGO
    "\u0F0A",	//TIBETAN MARK BKA
    "\u0F0B",	//TIBETAN MARK INTERSYLLABIC TSHEG
    "\u0F0C",	//TIBETAN MARK DELIMITER TSHEG BSTAR
    "\u0F0D",	//TIBETAN MARK SHAD
    "\u0F0E",	//TIBETAN MARK NYIS SHAD
    "\u0F0F",	//TIBETAN MARK TSHEG SHAD
    "\u0F10",	//TIBETAN MARK NYIS TSHEG SHAD
    "\u0F11",	//TIBETAN MARK RIN CHEN SPUNGS SHAD
    "\u0F12",	//TIBETAN MARK RGYA GRAM SHAD
    "\u0F14",	//TIBETAN MARK GTER TSHEG
    "\u0F85",	//TIBETAN MARK PALUTA
    "\u0FD0",	//TIBETAN MARK BSKA
    "\u0FD1",	//TIBETAN MARK MNYAM YIG GI MGO RGYAN
    "\u0FD2",	//TIBETAN MARK NYIS TSHEG
    "\u0FD3",	//TIBETAN MARK INITIAL BRDA RNYING YIG MGO MDUN MA
    "\u0FD4",	//TIBETAN MARK CLOSING BRDA RNYING YIG MGO SGAB MA
    "\u0FD9",	//TIBETAN MARK LEADING MCHAN RTAGS
    "\u0FDA",	//TIBETAN MARK TRAILING MCHAN RTAGS
    "\u104A",	//MYANMAR SIGN LITTLE SECTION
    "\u104B",	//MYANMAR SIGN SECTION
    "\u104C",	//MYANMAR SYMBOL LOCATIVE
    "\u104D",	//MYANMAR SYMBOL COMPLETED
    "\u104E",	//MYANMAR SYMBOL AFOREMENTIONED
    "\u104F",	//MYANMAR SYMBOL GENITIVE
    "\u10FB",	//GEORGIAN PARAGRAPH SEPARATOR
    "\u1360",	//ETHIOPIC SECTION MARK
    "\u1361",	//ETHIOPIC WORDSPACE
    "\u1362",	//ETHIOPIC FULL STOP
    "\u1363",	//ETHIOPIC COMMA
    "\u1364",	//ETHIOPIC SEMICOLON
    "\u1365",	//ETHIOPIC COLON
    "\u1366",	//ETHIOPIC PREFACE COLON
    "\u1367",	//ETHIOPIC QUESTION MARK
    "\u1368",	//ETHIOPIC PARAGRAPH SEPARATOR
    "\u166D",	//CANADIAN SYLLABICS CHI SIGN
    "\u166E",	//CANADIAN SYLLABICS FULL STOP
    "\u16EB",	//RUNIC SINGLE PUNCTUATION
    "\u16EC",	//RUNIC MULTIPLE PUNCTUATION
    "\u16ED",	//RUNIC CROSS PUNCTUATION
    "\u1735",	//PHILIPPINE SINGLE PUNCTUATION
    "\u1736",	//PHILIPPINE DOUBLE PUNCTUATION
    "\u17D4",	//KHMER SIGN KHAN
    "\u17D5",	//KHMER SIGN BARIYOOSAN
    "\u17D6",	//KHMER SIGN CAMNUC PII KUUH
    "\u17D8",	//KHMER SIGN BEYYAL
    "\u17D9",	//KHMER SIGN PHNAEK MUAN
    "\u17DA",	//KHMER SIGN KOOMUUT
    "\u1800",	//MONGOLIAN BIRGA
    "\u1801",	//MONGOLIAN ELLIPSIS
    "\u1802",	//MONGOLIAN COMMA
    "\u1803",	//MONGOLIAN FULL STOP
    "\u1804",	//MONGOLIAN COLON
    "\u1805",	//MONGOLIAN FOUR DOTS
    "\u1807",	//MONGOLIAN SIBE SYLLABLE BOUNDARY MARKER
    "\u1808",	//MONGOLIAN MANCHU COMMA
    "\u1809",	//MONGOLIAN MANCHU FULL STOP
    "\u180A",	//MONGOLIAN NIRUGU
    "\u1944",	//LIMBU EXCLAMATION MARK
    "\u1945",	//LIMBU QUESTION MARK
    "\u1A1E",	//BUGINESE PALLAWA
    "\u1A1F",	//BUGINESE END OF SECTION
    "\u1AA0",	//TAI THAM SIGN WIANG
    "\u1AA1",	//TAI THAM SIGN WIANGWAAK
    "\u1AA2",	//TAI THAM SIGN SAWAN
    "\u1AA3",	//TAI THAM SIGN KEOW
    "\u1AA4",	//TAI THAM SIGN HOY
    "\u1AA5",	//TAI THAM SIGN DOKMAI
    "\u1AA6",	//TAI THAM SIGN REVERSED ROTATED RANA
    "\u1AA8",	//TAI THAM SIGN KAAN
    "\u1AA9",	//TAI THAM SIGN KAANKUU
    "\u1AAA",	//TAI THAM SIGN SATKAAN
    "\u1AAB",	//TAI THAM SIGN SATKAANKUU
    "\u1AAC",	//TAI THAM SIGN HANG
    "\u1AAD",	//TAI THAM SIGN CAANG
    "\u1B5A",	//BALINESE PANTI
    "\u1B5B",	//BALINESE PAMADA
    "\u1B5C",	//BALINESE WINDU
    "\u1B5D",	//BALINESE CARIK PAMUNGKAH
    "\u1B5E",	//BALINESE CARIK SIKI
    "\u1B5F",	//BALINESE CARIK PAREREN
    "\u1B60",	//BALINESE PAMENENG
    "\u1BFC",	//BATAK SYMBOL BINDU NA METEK
    "\u1BFD",	//BATAK SYMBOL BINDU PINARBORAS
    "\u1BFE",	//BATAK SYMBOL BINDU JUDUL
    "\u1BFF",	//BATAK SYMBOL BINDU PANGOLAT
    "\u1C3B",	//LEPCHA PUNCTUATION TA
    "\u1C3C",	//LEPCHA PUNCTUATION NYET THYOOM TA
    "\u1C3D",	//LEPCHA PUNCTUATION CER
    "\u1C3E",	//LEPCHA PUNCTUATION TSHOOK CER
    "\u1C3F",	//LEPCHA PUNCTUATION TSHOOK
    "\u1C7E",	//OL CHIKI PUNCTUATION MUCAAD
    "\u1C7F",	//OL CHIKI PUNCTUATION DOUBLE MUCAAD
    "\u1CC0",	//SUNDANESE PUNCTUATION BINDU SURYA
    "\u1CC1",	//SUNDANESE PUNCTUATION BINDU PANGLONG
    "\u1CC2",	//SUNDANESE PUNCTUATION BINDU PURNAMA
    "\u1CC3",	//SUNDANESE PUNCTUATION BINDU CAKRA
    "\u1CC4",	//SUNDANESE PUNCTUATION BINDU LEU SATANGA
    "\u1CC5",	//SUNDANESE PUNCTUATION BINDU KA SATANGA
    "\u1CC6",	//SUNDANESE PUNCTUATION BINDU DA SATANGA
    "\u1CC7",	//SUNDANESE PUNCTUATION BINDU BA SATANGA
    "\u1CD3",	//VEDIC SIGN NIHSHVASA
    "\u2016",	//DOUBLE VERTICAL LINE
    "\u2017",	//DOUBLE LOW LINE
    "\u2020",	//DAGGER
    "\u2021",	//DOUBLE DAGGER
    "\u2022",	//BULLET
    "\u2023",	//TRIANGULAR BULLET
    "\u2024",	//ONE DOT LEADER
    "\u2025",	//TWO DOT LEADER
    "\u2026",	//HORIZONTAL ELLIPSIS
    "\u2027",	//HYPHENATION POINT
    "\u2030",	//PER MILLE SIGN
    "\u2031",	//PER TEN THOUSAND SIGN
    "\u2032",	//PRIME
    "\u2033",	//DOUBLE PRIME
    "\u2034",	//TRIPLE PRIME
    "\u2035",	//REVERSED PRIME
    "\u2036",	//REVERSED DOUBLE PRIME
    "\u2037",	//REVERSED TRIPLE PRIME
    "\u2038",	//CARET
    "\u203B",	//REFERENCE MARK
    "\u203C",	//DOUBLE EXCLAMATION MARK
    "\u203D",	//INTERROBANG
    "\u203E",	//OVERLINE
    "\u2041",	//CARET INSERTION POINT
    "\u2042",	//ASTERISM
    "\u2043",	//HYPHEN BULLET
    "\u2047",	//DOUBLE QUESTION MARK
    "\u2048",	//QUESTION EXCLAMATION MARK
    "\u2049",	//EXCLAMATION QUESTION MARK
    "\u204A",	//TIRONIAN SIGN ET
    "\u204B",	//REVERSED PILCROW SIGN
    "\u204C",	//BLACK LEFTWARDS BULLET
    "\u204D",	//BLACK RIGHTWARDS BULLET
    "\u204E",	//LOW ASTERISK
    "\u204F",	//REVERSED SEMICOLON
    "\u2050",	//CLOSE UP
    "\u2051",	//TWO ASTERISKS ALIGNED VERTICALLY
    "\u2053",	//SWUNG DASH
    "\u2055",	//FLOWER PUNCTUATION MARK
    "\u2056",	//THREE DOT PUNCTUATION
    "\u2057",	//QUADRUPLE PRIME
    "\u2058",	//FOUR DOT PUNCTUATION
    "\u2059",	//FIVE DOT PUNCTUATION
    "\u205A",	//TWO DOT PUNCTUATION
    "\u205B",	//FOUR DOT MARK
    "\u205C",	//DOTTED CROSS
    "\u205D",	//TRICOLON
    "\u205E",	//VERTICAL FOUR DOTS
    "\u2CF9",	//COPTIC OLD NUBIAN FULL STOP
    "\u2CFA",	//COPTIC OLD NUBIAN DIRECT QUESTION MARK
    "\u2CFB",	//COPTIC OLD NUBIAN INDIRECT QUESTION MARK
    "\u2CFC",	//COPTIC OLD NUBIAN VERSE DIVIDER
    "\u2CFE",	//COPTIC FULL STOP
    "\u2CFF",	//COPTIC MORPHOLOGICAL DIVIDER
    "\u2D70",	//TIFINAGH SEPARATOR MARK
    "\u2E00",	//RIGHT ANGLE SUBSTITUTION MARKER
    "\u2E01",	//RIGHT ANGLE DOTTED SUBSTITUTION MARKER
    "\u2E06",	//RAISED INTERPOLATION MARKER
    "\u2E07",	//RAISED DOTTED INTERPOLATION MARKER
    "\u2E08",	//DOTTED TRANSPOSITION MARKER
    "\u2E0B",	//RAISED SQUARE
    "\u2E0E",	//EDITORIAL CORONIS
    "\u2E0F",	//PARAGRAPHOS
    "\u2E10",	//FORKED PARAGRAPHOS
    "\u2E11",	//REVERSED FORKED PARAGRAPHOS
    "\u2E12",	//HYPODIASTOLE
    "\u2E13",	//DOTTED OBELOS
    "\u2E14",	//DOWNWARDS ANCORA
    "\u2E15",	//UPWARDS ANCORA
    "\u2E16",	//DOTTED RIGHT
    "\u2E18",	//INVERTED INTERROBANG
    "\u2E19",	//PALM BRANCH
    "\u2E1B",	//TILDE WITH RING ABOVE
    "\u2E1E",	//TILDE WITH DOT ABOVE
    "\u2E1F",	//TILDE WITH DOT BELOW
    "\u2E2A",	//TWO DOTS OVER ONE DOT PUNCTUATION
    "\u2E2B",	//ONE DOT OVER TWO DOTS PUNCTUATION
    "\u2E2C",	//SQUARED FOUR DOT PUNCTUATION
    "\u2E2D",	//FIVE DOT MARK
    "\u2E2E",	//REVERSED QUESTION MARK
    "\u2E30",	//RING POINT
    "\u2E31",	//WORD SEPARATOR MIDDLE DOT
    "\u2E32",	//TURNED COMMA
    "\u2E33",	//RAISED DOT
    "\u2E34",	//RAISED COMMA
    "\u2E35",	//TURNED SEMICOLON
    "\u2E36",	//DAGGER WITH LEFT GUARD
    "\u2E37",	//DAGGER WITH RIGHT GUARD
    "\u2E38",	//TURNED DAGGER
    "\u2E39",	//TOP HALF SECTION SIGN
    "\u2E3C",	//STENOGRAPHIC FULL STOP
    "\u2E3D",	//VERTICAL SIX DOTS
    "\u2E3E",	//WIGGLY VERTICAL LINE
    "\u2E3F",	//CAPITULUM
    "\u2E41",	//REVERSED COMMA
    "\u3001",	//IDEOGRAPHIC COMMA
    "\u3002",	//IDEOGRAPHIC FULL STOP
    "\u3003",	//DITTO MARK
    "\u303D",	//PART ALTERNATION MARK
    "\u30FB",	//KATAKANA MIDDLE DOT
    "\uA4FE",	//LISU PUNCTUATION COMMA
    "\uA4FF",	//LISU PUNCTUATION FULL STOP
    "\uA60D",	//VAI COMMA
    "\uA60E",	//VAI FULL STOP
    "\uA60F",	//VAI QUESTION MARK
    "\uA673",	//SLAVONIC ASTERISK
    "\uA67E",	//CYRILLIC KAVYKA
    "\uA6F2",	//BAMUM NJAEMLI
    "\uA6F3",	//BAMUM FULL STOP
    "\uA6F4",	//BAMUM COLON
    "\uA6F5",	//BAMUM COMMA
    "\uA6F6",	//BAMUM SEMICOLON
    "\uA6F7",	//BAMUM QUESTION MARK
    "\uA874",	//PHAGS
    "\uA875",	//PHAGS
    "\uA876",	//PHAGS
    "\uA877",	//PHAGS
    "\uA8CE",	//SAURASHTRA DANDA
    "\uA8CF",	//SAURASHTRA DOUBLE DANDA
    "\uA8F8",	//DEVANAGARI SIGN PUSHPIKA
    "\uA8F9",	//DEVANAGARI GAP FILLER
    "\uA8FA",	//DEVANAGARI CARET
    "\uA8FC",	//DEVANAGARI SIGN SIDDHAM
    "\uA92E",	//KAYAH LI SIGN CWI
    "\uA92F",	//KAYAH LI SIGN SHYA
    "\uA95F",	//REJANG SECTION MARK
    "\uA9C1",	//JAVANESE LEFT RERENGGAN
    "\uA9C2",	//JAVANESE RIGHT RERENGGAN
    "\uA9C3",	//JAVANESE PADA ANDAP
    "\uA9C4",	//JAVANESE PADA MADYA
    "\uA9C5",	//JAVANESE PADA LUHUR
    "\uA9C6",	//JAVANESE PADA WINDU
    "\uA9C7",	//JAVANESE PADA PANGKAT
    "\uA9C8",	//JAVANESE PADA LINGSA
    "\uA9C9",	//JAVANESE PADA LUNGSI
    "\uA9CA",	//JAVANESE PADA ADEG
    "\uA9CB",	//JAVANESE PADA ADEG ADEG
    "\uA9CC",	//JAVANESE PADA PISELEH
    "\uA9CD",	//JAVANESE TURNED PADA PISELEH
    "\uA9DE",	//JAVANESE PADA TIRTA TUMETES
    "\uA9DF",	//JAVANESE PADA ISEN
    "\uAA5C",	//CHAM PUNCTUATION SPIRAL
    "\uAA5D",	//CHAM PUNCTUATION DANDA
    "\uAA5E",	//CHAM PUNCTUATION DOUBLE DANDA
    "\uAA5F",	//CHAM PUNCTUATION TRIPLE DANDA
    "\uAADE",	//TAI VIET SYMBOL HO HOI
    "\uAADF",	//TAI VIET SYMBOL KOI KOI
    "\uAAF0",	//MEETEI MAYEK CHEIKHAN
    "\uAAF1",	//MEETEI MAYEK AHANG KHUDAM
    "\uABEB",	//MEETEI MAYEK CHEIKHEI
    "\uFE10",	//PRESENTATION FORM FOR VERTICAL COMMA
    "\uFE11",	//PRESENTATION FORM FOR VERTICAL IDEOGRAPHIC COMMA
    "\uFE12",	//PRESENTATION FORM FOR VERTICAL IDEOGRAPHIC FULL STOP
    "\uFE13",	//PRESENTATION FORM FOR VERTICAL COLON
    "\uFE14",	//PRESENTATION FORM FOR VERTICAL SEMICOLON
    "\uFE15",	//PRESENTATION FORM FOR VERTICAL EXCLAMATION MARK
    "\uFE16",	//PRESENTATION FORM FOR VERTICAL QUESTION MARK
    "\uFE19",	//PRESENTATION FORM FOR VERTICAL HORIZONTAL ELLIPSIS
    "\uFE30",	//PRESENTATION FORM FOR VERTICAL TWO DOT LEADER
    "\uFE45",	//SESAME DOT
    "\uFE46",	//WHITE SESAME DOT
    "\uFE49",	//DASHED OVERLINE
    "\uFE4A",	//CENTRELINE OVERLINE
    "\uFE4B",	//WAVY OVERLINE
    "\uFE4C",	//DOUBLE WAVY OVERLINE
    "\uFE50",	//SMALL COMMA
    "\uFE51",	//SMALL IDEOGRAPHIC COMMA
    "\uFE52",	//SMALL FULL STOP
    "\uFE54",	//SMALL SEMICOLON
    "\uFE55",	//SMALL COLON
    "\uFE56",	//SMALL QUESTION MARK
    "\uFE57",	//SMALL EXCLAMATION MARK
    "\uFE5F",	//SMALL NUMBER SIGN
    "\uFE60",	//SMALL AMPERSAND
    "\uFE61",	//SMALL ASTERISK
    "\uFE68",	//SMALL REVERSE SOLIDUS
    "\uFE6A",	//SMALL PERCENT SIGN
    "\uFE6B",	//SMALL COMMERCIAL AT
    "\uFF01",	//FULLWIDTH EXCLAMATION MARK
    "\uFF02",	//FULLWIDTH QUOTATION MARK
    "\uFF03",	//FULLWIDTH NUMBER SIGN
    "\uFF05",	//FULLWIDTH PERCENT SIGN
    "\uFF06",	//FULLWIDTH AMPERSAND
    "\uFF07",	//FULLWIDTH APOSTROPHE
    "\uFF0A",	//FULLWIDTH ASTERISK
    "\uFF0C",	//FULLWIDTH COMMA
    "\uFF0E",	//FULLWIDTH FULL STOP
    "\uFF0F",	//FULLWIDTH SOLIDUS
    "\uFF1A",	//FULLWIDTH COLON
    "\uFF1B",	//FULLWIDTH SEMICOLON
    "\uFF1F",	//FULLWIDTH QUESTION MARK
    "\uFF20",	//FULLWIDTH COMMERCIAL AT
    "\uFF3C",	//FULLWIDTH REVERSE SOLIDUS
    "\uFF61",	//HALFWIDTH IDEOGRAPHIC FULL STOP
    "\uFF64",	//HALFWIDTH IDEOGRAPHIC COMMA
    "\uFF65",	//HALFWIDTH KATAKANA MIDDLE DOT
    "\u10100",	//AEGEAN WORD SEPARATOR LINE
    "\u10101",	//AEGEAN WORD SEPARATOR DOT
    "\u10102",	//AEGEAN CHECK MARK
    "\u1039F",	//UGARITIC WORD DIVIDER
    "\u103D0",	//OLD PERSIAN WORD DIVIDER
    "\u1056F",	//CAUCASIAN ALBANIAN CITATION MARK
    "\u10857",	//IMPERIAL ARAMAIC SECTION SIGN
    "\u1091F",	//PHOENICIAN WORD SEPARATOR
    "\u1093F",	//LYDIAN TRIANGULAR MARK
    "\u10A50",	//KHAROSHTHI PUNCTUATION DOT
    "\u10A51",	//KHAROSHTHI PUNCTUATION SMALL CIRCLE
    "\u10A52",	//KHAROSHTHI PUNCTUATION CIRCLE
    "\u10A53",	//KHAROSHTHI PUNCTUATION CRESCENT BAR
    "\u10A54",	//KHAROSHTHI PUNCTUATION MANGALAM
    "\u10A55",	//KHAROSHTHI PUNCTUATION LOTUS
    "\u10A56",	//KHAROSHTHI PUNCTUATION DANDA
    "\u10A57",	//KHAROSHTHI PUNCTUATION DOUBLE DANDA
    "\u10A58",	//KHAROSHTHI PUNCTUATION LINES
    "\u10A7F",	//OLD SOUTH ARABIAN NUMERIC INDICATOR
    "\u10AF0",	//MANICHAEAN PUNCTUATION STAR
    "\u10AF1",	//MANICHAEAN PUNCTUATION FLEURON
    "\u10AF2",	//MANICHAEAN PUNCTUATION DOUBLE DOT WITHIN DOT
    "\u10AF3",	//MANICHAEAN PUNCTUATION DOT WITHIN DOT
    "\u10AF4",	//MANICHAEAN PUNCTUATION DOT
    "\u10AF5",	//MANICHAEAN PUNCTUATION TWO DOTS
    "\u10AF6",	//MANICHAEAN PUNCTUATION LINE FILLER
    "\u10B39",	//AVESTAN ABBREVIATION MARK
    "\u10B3A",	//TINY TWO DOTS OVER ONE DOT PUNCTUATION
    "\u10B3B",	//SMALL TWO DOTS OVER ONE DOT PUNCTUATION
    "\u10B3C",	//LARGE TWO DOTS OVER ONE DOT PUNCTUATION
    "\u10B3D",	//LARGE ONE DOT OVER TWO DOTS PUNCTUATION
    "\u10B3E",	//LARGE TWO RINGS OVER ONE RING PUNCTUATION
    "\u10B3F",	//LARGE ONE RING OVER TWO RINGS PUNCTUATION
    "\u10B99",	//PSALTER PAHLAVI SECTION MARK
    "\u10B9A",	//PSALTER PAHLAVI TURNED SECTION MARK
    "\u10B9B",	//PSALTER PAHLAVI FOUR DOTS WITH CROSS
    "\u10B9C",	//PSALTER PAHLAVI FOUR DOTS WITH DOT
    "\u11047",	//BRAHMI DANDA
    "\u11048",	//BRAHMI DOUBLE DANDA
    "\u11049",	//BRAHMI PUNCTUATION DOT
    "\u1104A",	//BRAHMI PUNCTUATION DOUBLE DOT
    "\u1104B",	//BRAHMI PUNCTUATION LINE
    "\u1104C",	//BRAHMI PUNCTUATION CRESCENT BAR
    "\u1104D",	//BRAHMI PUNCTUATION LOTUS
    "\u110BB",	//KAITHI ABBREVIATION SIGN
    "\u110BC",	//KAITHI ENUMERATION SIGN
    "\u110BE",	//KAITHI SECTION MARK
    "\u110BF",	//KAITHI DOUBLE SECTION MARK
    "\u110C0",	//KAITHI DANDA
    "\u110C1",	//KAITHI DOUBLE DANDA
    "\u11140",	//CHAKMA SECTION MARK
    "\u11141",	//CHAKMA DANDA
    "\u11142",	//CHAKMA DOUBLE DANDA
    "\u11143",	//CHAKMA QUESTION MARK
    "\u11174",	//MAHAJANI ABBREVIATION SIGN
    "\u11175",	//MAHAJANI SECTION MARK
    "\u111C5",	//SHARADA DANDA
    "\u111C6",	//SHARADA DOUBLE DANDA
    "\u111C7",	//SHARADA ABBREVIATION SIGN
    "\u111C8",	//SHARADA SEPARATOR
    "\u111C9",	//SHARADA SANDHI MARK
    "\u111CD",	//SHARADA SUTRA MARK
    "\u111DB",	//SHARADA SIGN SIDDHAM
    "\u111DD",	//SHARADA CONTINUATION SIGN
    "\u111DE",	//SHARADA SECTION MARK
    "\u111DF",	//SHARADA SECTION MARK
    "\u11238",	//KHOJKI DANDA
    "\u11239",	//KHOJKI DOUBLE DANDA
    "\u1123A",	//KHOJKI WORD SEPARATOR
    "\u1123B",	//KHOJKI SECTION MARK
    "\u1123C",	//KHOJKI DOUBLE SECTION MARK
    "\u1123D",	//KHOJKI ABBREVIATION SIGN
    "\u112A9",	//MULTANI SECTION MARK
    "\u114C6",	//TIRHUTA ABBREVIATION SIGN
    "\u115C1",	//SIDDHAM SIGN SIDDHAM
    "\u115C2",	//SIDDHAM DANDA
    "\u115C3",	//SIDDHAM DOUBLE DANDA
    "\u115C4",	//SIDDHAM SEPARATOR DOT
    "\u115C5",	//SIDDHAM SEPARATOR BAR
    "\u115C6",	//SIDDHAM REPETITION MARK
    "\u115C7",	//SIDDHAM REPETITION MARK
    "\u115C8",	//SIDDHAM REPETITION MARK
    "\u115C9",	//SIDDHAM END OF TEXT MARK
    "\u115CA",	//SIDDHAM SECTION MARK WITH TRIDENT AND U
    "\u115CB",	//SIDDHAM SECTION MARK WITH TRIDENT AND DOTTED CRESCENTS
    "\u115CC",	//SIDDHAM SECTION MARK WITH RAYS AND DOTTED CRESCENTS
    "\u115CD",	//SIDDHAM SECTION MARK WITH RAYS AND DOTTED DOUBLE CRESCENTS
    "\u115CE",	//SIDDHAM SECTION MARK WITH RAYS AND DOTTED TRIPLE CRESCENTS
    "\u115CF",	//SIDDHAM SECTION MARK DOUBLE RING
    "\u115D0",	//SIDDHAM SECTION MARK DOUBLE RING WITH RAYS
    "\u115D1",	//SIDDHAM SECTION MARK WITH DOUBLE CRESCENTS
    "\u115D2",	//SIDDHAM SECTION MARK WITH TRIPLE CRESCENTS
    "\u115D3",	//SIDDHAM SECTION MARK WITH QUADRUPLE CRESCENTS
    "\u115D4",	//SIDDHAM SECTION MARK WITH SEPTUPLE CRESCENTS
    "\u115D5",	//SIDDHAM SECTION MARK WITH CIRCLES AND RAYS
    "\u115D6",	//SIDDHAM SECTION MARK WITH CIRCLES AND TWO ENCLOSURES
    "\u115D7",	//SIDDHAM SECTION MARK WITH CIRCLES AND FOUR ENCLOSURES
    "\u11641",	//MODI DANDA
    "\u11642",	//MODI DOUBLE DANDA
    "\u11643",	//MODI ABBREVIATION SIGN
    "\u1173C",	//AHOM SIGN SMALL SECTION
    "\u1173D",	//AHOM SIGN SECTION
    "\u1173E",	//AHOM SIGN RULAI
    "\u12470",	//CUNEIFORM PUNCTUATION SIGN OLD ASSYRIAN WORD DIVIDER
    "\u12471",	//CUNEIFORM PUNCTUATION SIGN VERTICAL COLON
    "\u12472",	//CUNEIFORM PUNCTUATION SIGN DIAGONAL COLON
    "\u12473",	//CUNEIFORM PUNCTUATION SIGN DIAGONAL TRICOLON
    "\u12474",	//CUNEIFORM PUNCTUATION SIGN DIAGONAL QUADCOLON
    "\u16A6E",	//MRO DANDA
    "\u16A6F",	//MRO DOUBLE DANDA
    "\u16AF5",	//BASSA VAH FULL STOP
    "\u16B37",	//PAHAWH HMONG SIGN VOS THOM
    "\u16B38",	//PAHAWH HMONG SIGN VOS TSHAB CEEB
    "\u16B39",	//PAHAWH HMONG SIGN CIM CHEEM
    "\u16B3A",	//PAHAWH HMONG SIGN VOS THIAB
    "\u16B3B",	//PAHAWH HMONG SIGN VOS FEEM
    "\u16B44",	//PAHAWH HMONG SIGN XAUS
    "\u1BC9F",	//DUPLOYAN PUNCTUATION CHINOOK FULL STOP
    "\u1DA87",	//SIGNWRITING COMMA
    "\u1DA88",	//SIGNWRITING FULL STOP
    "\u1DA89",	//SIGNWRITING SEMICOLON
    "\u1DA8A",	//SIGNWRITING COLON
    "\u1DA8B",	//SIGNWRITING PARENTHESIS

    // Open Punction (Ps)
    "\u0028",	//LEFT PARENTHESIS
    "\u005B",	//LEFT SQUARE BRACKET
    "\u007B",	//LEFT CURLY BRACKET
    "\u0F3A",	//TIBETAN MARK GUG RTAGS GYON
    "\u0F3C",	//TIBETAN MARK ANG KHANG GYON
    "\u169B",	//OGHAM FEATHER MARK
    "\u201A",	//SINGLE LOW
    "\u201E",	//DOUBLE LOW
    "\u2045",	//LEFT SQUARE BRACKET WITH QUILL
    "\u207D",	//SUPERSCRIPT LEFT PARENTHESIS
    "\u208D",	//SUBSCRIPT LEFT PARENTHESIS
    "\u2308",	//LEFT CEILING
    "\u230A",	//LEFT FLOOR
    "\u2329",	//LEFT
    "\u2768",	//MEDIUM LEFT PARENTHESIS ORNAMENT
    "\u276A",	//MEDIUM FLATTENED LEFT PARENTHESIS ORNAMENT
    "\u276C",	//MEDIUM LEFT
    "\u276E",	//HEAVY LEFT
    "\u2770",	//HEAVY LEFT
    "\u2772",	//LIGHT LEFT TORTOISE SHELL BRACKET ORNAMENT
    "\u2774",	//MEDIUM LEFT CURLY BRACKET ORNAMENT
    "\u27C5",	//LEFT S
    "\u27E6",	//MATHEMATICAL LEFT WHITE SQUARE BRACKET
    "\u27E8",	//MATHEMATICAL LEFT ANGLE BRACKET
    "\u27EA",	//MATHEMATICAL LEFT DOUBLE ANGLE BRACKET
    "\u27EC",	//MATHEMATICAL LEFT WHITE TORTOISE SHELL BRACKET
    "\u27EE",	//MATHEMATICAL LEFT FLATTENED PARENTHESIS
    "\u2983",	//LEFT WHITE CURLY BRACKET
    "\u2985",	//LEFT WHITE PARENTHESIS
    "\u2987",	//Z NOTATION LEFT IMAGE BRACKET
    "\u2989",	//Z NOTATION LEFT BINDING BRACKET
    "\u298B",	//LEFT SQUARE BRACKET WITH UNDERBAR
    "\u298D",	//LEFT SQUARE BRACKET WITH TICK IN TOP CORNER
    "\u298F",	//LEFT SQUARE BRACKET WITH TICK IN BOTTOM CORNER
    "\u2991",	//LEFT ANGLE BRACKET WITH DOT
    "\u2993",	//LEFT ARC LESS
    "\u2995",	//DOUBLE LEFT ARC GREATER
    "\u2997",	//LEFT BLACK TORTOISE SHELL BRACKET
    "\u29D8",	//LEFT WIGGLY FENCE
    "\u29DA",	//LEFT DOUBLE WIGGLY FENCE
    "\u29FC",	//LEFT
    "\u2E22",	//TOP LEFT HALF BRACKET
    "\u2E24",	//BOTTOM LEFT HALF BRACKET
    "\u2E26",	//LEFT SIDEWAYS U BRACKET
    "\u2E28",	//LEFT DOUBLE PARENTHESIS
    "\u2E42",	//DOUBLE LOW
    "\u3008",	//LEFT ANGLE BRACKET
    "\u300A",	//LEFT DOUBLE ANGLE BRACKET
    "\u300C",	//LEFT CORNER BRACKET
    "\u300E",	//LEFT WHITE CORNER BRACKET
    "\u3010",	//LEFT BLACK LENTICULAR BRACKET
    "\u3014",	//LEFT TORTOISE SHELL BRACKET
    "\u3016",	//LEFT WHITE LENTICULAR BRACKET
    "\u3018",	//LEFT WHITE TORTOISE SHELL BRACKET
    "\u301A",	//LEFT WHITE SQUARE BRACKET
    "\u301D",	//REVERSED DOUBLE PRIME QUOTATION MARK
    "\uFD3F",	//ORNATE RIGHT PARENTHESIS
    "\uFE17",	//PRESENTATION FORM FOR VERTICAL LEFT WHITE LENTICULAR BRACKET
    "\uFE35",	//PRESENTATION FORM FOR VERTICAL LEFT PARENTHESIS
    "\uFE37",	//PRESENTATION FORM FOR VERTICAL LEFT CURLY BRACKET
    "\uFE39",	//PRESENTATION FORM FOR VERTICAL LEFT TORTOISE SHELL BRACKET
    "\uFE3B",	//PRESENTATION FORM FOR VERTICAL LEFT BLACK LENTICULAR BRACKET
    "\uFE3D",	//PRESENTATION FORM FOR VERTICAL LEFT DOUBLE ANGLE BRACKET
    "\uFE3F",	//PRESENTATION FORM FOR VERTICAL LEFT ANGLE BRACKET
    "\uFE41",	//PRESENTATION FORM FOR VERTICAL LEFT CORNER BRACKET
    "\uFE43",	//PRESENTATION FORM FOR VERTICAL LEFT WHITE CORNER BRACKET
    "\uFE47",	//PRESENTATION FORM FOR VERTICAL LEFT SQUARE BRACKET
    "\uFE59",	//SMALL LEFT PARENTHESIS
    "\uFE5B",	//SMALL LEFT CURLY BRACKET
    "\uFE5D",	//SMALL LEFT TORTOISE SHELL BRACKET
    "\uFF08",	//FULLWIDTH LEFT PARENTHESIS
    "\uFF3B",	//FULLWIDTH LEFT SQUARE BRACKET
    "\uFF5B",	//FULLWIDTH LEFT CURLY BRACKET
    "\uFF5F",	//FULLWIDTH LEFT WHITE PARENTHESIS
    "\uFF62"	//HALFWIDTH LEFT CORNER BRACKET
  )
}