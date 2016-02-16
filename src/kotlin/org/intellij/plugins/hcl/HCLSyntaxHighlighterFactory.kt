/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.intellij.plugins.hcl

import com.intellij.lexer.LayeredLexer
import com.intellij.lexer.Lexer
import com.intellij.lexer.StringLiteralLexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.*
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.StringEscapesTokenTypes
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import java.util.*


open class HCLSyntaxHighlighterFactory : SyntaxHighlighterFactory() {


  open class MySyntaxHighlighter(val lexer: HCLLexer) : SyntaxHighlighterBase() {
    val ourAttributes: Map<IElementType, TextAttributesKey> = HashMap()

    val HCL_BRACKETS: TextAttributesKey = TextAttributesKey.createTextAttributesKey("HCL.BRACKETS", BRACKETS)
    val HCL_BRACES: TextAttributesKey = TextAttributesKey.createTextAttributesKey("HCL.BRACES", BRACES)
    val HCL_COMMA: TextAttributesKey = TextAttributesKey.createTextAttributesKey("HCL.COMMA", COMMA)
    val HCL_COLON: TextAttributesKey = TextAttributesKey.createTextAttributesKey("HCL.COLON", SEMICOLON)
    val HCL_NUMBER: TextAttributesKey = TextAttributesKey.createTextAttributesKey("HCL.NUMBER", NUMBER)
    val HCL_STRING: TextAttributesKey = TextAttributesKey.createTextAttributesKey("HCL.STRING", STRING)
    val HCL_KEYWORD: TextAttributesKey = TextAttributesKey.createTextAttributesKey("HCL.KEYWORD", KEYWORD)
    val HCL_LINE_COMMENT: TextAttributesKey = TextAttributesKey.createTextAttributesKey("HCL.LINE_COMMENT", LINE_COMMENT)
    val HCL_BLOCK_COMMENT: TextAttributesKey = TextAttributesKey.createTextAttributesKey("HCL.BLOCK_COMMENT", BLOCK_COMMENT)

    // Artificial element type
    val HCL_IDENTIFIER: TextAttributesKey = TextAttributesKey.createTextAttributesKey("HCL.IDENTIFIER", IDENTIFIER)

    // Added by annotators
    val HCL_PROPERTY_KEY: TextAttributesKey = TextAttributesKey.createTextAttributesKey("HCL.PROPERTY_KEY", INSTANCE_FIELD)

    // String escapes
    val HCL_VALID_ESCAPE: TextAttributesKey = TextAttributesKey.createTextAttributesKey("HCL.VALID_ESCAPE", VALID_STRING_ESCAPE)
    val HCL_INVALID_ESCAPE: TextAttributesKey = TextAttributesKey.createTextAttributesKey("HCL.INVALID_ESCAPE", INVALID_STRING_ESCAPE)


    init {
      SyntaxHighlighterBase.fillMap(ourAttributes, HCL_BRACES, HCLElementTypes.L_CURLY, HCLElementTypes.R_CURLY)
      SyntaxHighlighterBase.fillMap(ourAttributes, HCL_BRACKETS, HCLElementTypes.L_BRACKET, HCLElementTypes.R_BRACKET)
      SyntaxHighlighterBase.fillMap(ourAttributes, HCL_COMMA, HCLElementTypes.COMMA)
      //      SyntaxHighlighterBase.fillMap(ourAttributes, HCL_COLON, HCLElementTypes.COLON)
      SyntaxHighlighterBase.fillMap(ourAttributes, HCL_STRING, HCLElementTypes.DOUBLE_QUOTED_STRING)
      SyntaxHighlighterBase.fillMap(ourAttributes, HCL_STRING, HCLElementTypes.SINGLE_QUOTED_STRING)
      SyntaxHighlighterBase.fillMap(ourAttributes, HCL_NUMBER, HCLElementTypes.NUMBER)
      SyntaxHighlighterBase.fillMap(ourAttributes, HCL_KEYWORD, HCLElementTypes.TRUE, HCLElementTypes.FALSE, HCLElementTypes.NULL)
      SyntaxHighlighterBase.fillMap(ourAttributes, HCL_LINE_COMMENT, HCLElementTypes.LINE_COMMENT)
      SyntaxHighlighterBase.fillMap(ourAttributes, HCL_BLOCK_COMMENT, HCLElementTypes.BLOCK_COMMENT)
      // TODO may be it's worth to add more sensible highlighting for identifiers
      SyntaxHighlighterBase.fillMap(ourAttributes, HCL_IDENTIFIER, HCLElementTypes.ID)
      SyntaxHighlighterBase.fillMap(ourAttributes, HighlighterColors.BAD_CHARACTER, TokenType.BAD_CHARACTER)

      SyntaxHighlighterBase.fillMap(ourAttributes, HCL_VALID_ESCAPE, StringEscapesTokenTypes.VALID_STRING_ESCAPE_TOKEN)
      SyntaxHighlighterBase.fillMap(ourAttributes, HCL_INVALID_ESCAPE, StringEscapesTokenTypes.INVALID_CHARACTER_ESCAPE_TOKEN)
      SyntaxHighlighterBase.fillMap(ourAttributes, HCL_INVALID_ESCAPE, StringEscapesTokenTypes.INVALID_UNICODE_ESCAPE_TOKEN)
    }


    override fun getTokenHighlights(tokenType: IElementType?): Array<out TextAttributesKey> {
      return SyntaxHighlighterBase.pack(ourAttributes[tokenType])
    }

    override fun getHighlightingLexer(): Lexer {
      val layeredLexer = LayeredLexer(lexer)

      layeredLexer.registerSelfStoppingLayer(StringLiteralLexer('\"', HCLElementTypes.DOUBLE_QUOTED_STRING, false, "/", false, false), arrayOf(HCLElementTypes.DOUBLE_QUOTED_STRING), IElementType.EMPTY_ARRAY)
      layeredLexer.registerSelfStoppingLayer(StringLiteralLexer('\'', HCLElementTypes.SINGLE_QUOTED_STRING, false, "/", false, false), arrayOf(HCLElementTypes.SINGLE_QUOTED_STRING), IElementType.EMPTY_ARRAY)
      return layeredLexer
    }

  }

  override fun getSyntaxHighlighter(project: Project?, virtualFile: VirtualFile?): SyntaxHighlighter {
    return MySyntaxHighlighter(HCLLexer())
  }
}
