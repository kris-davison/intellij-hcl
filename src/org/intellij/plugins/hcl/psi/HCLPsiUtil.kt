package org.intellij.plugins.hcl.psi

import com.intellij.lang.ASTNode
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import org.intellij.plugins.hcl.HCLElementTypes
import org.intellij.plugins.hcl.HCLParserDefinition

/**
 * Various helper methods for working with PSI of JSON language.

 * @author Mikhail Golubev
 */
SuppressWarnings("UnusedDeclaration")
public object HCLPsiUtil {


  /**
   * Checks that PSI element represents item of JSON array.

   * @param element PSI element to check
   * *
   * @return whether this PSI element is array element
   */
  public fun isArrayElement(element: PsiElement): Boolean {
    return element is HCLValue && element.getParent() is HCLArray
  }

  /**
   * Checks that PSI element represents key of JSON property (key-value pair of JSON object)

   * @param element PSI element to check
   * *
   * @return whether this PSI element is property key
   */
  public fun isPropertyKey(element: PsiElement): Boolean {
    val parent = element.getParent()
    return parent is HCLProperty && element == parent.getNameElement()
  }

  /**
   * Checks that PSI element represents value of JSON property (key-value pair of JSON object)

   * @param element PSI element to check
   * *
   * @return whether this PSI element is property value
   */
  public fun isPropertyValue(element: PsiElement): Boolean {
    val parent = element.getParent()
    return parent is HCLProperty && element == parent.getValue()
  }

  /**
   * Find the furthest sibling element with the same type as given anchor.
   *
   *
   * Ignore white spaces for any type of element except [org.intellij.plugins.hcl.HCLElementTypes.LINE_COMMENT]
   * where non indentation white space (that has new line in the middle) will stop the search.

   * @param anchor element to start from
   * *
   * @param after  whether to scan through sibling elements forward or backward
   * *
   * @return described element or anchor if search stops immediately
   */
  public fun findFurthestSiblingOfSameType(anchor: PsiElement, after: Boolean): PsiElement {
    var node: ASTNode? = anchor.getNode()
    // Compare by node type to distinguish between different types of comments
    val expectedType = node!!.getElementType()
    var lastSeen: ASTNode = node
    while (node != null) {
      val elementType = node.getElementType()
      if (elementType == expectedType) {
        lastSeen = node
      } else if (elementType == TokenType.WHITE_SPACE) {
        if (expectedType == HCLElementTypes.LINE_COMMENT && node.getText().indexOf('\n', 1) != -1) {
          break
        }
      } else if (!HCLParserDefinition.HCL_COMMENTARIES.contains(elementType)
                || HCLParserDefinition.HCL_COMMENTARIES.contains(expectedType)) {
        break
      }
      node = if (after) node.getTreeNext() else node.getTreePrev()
    }
    return lastSeen.getPsi()
  }

  /**
   * Check that element type of the given AST node belongs to the token set.
   *
   *
   * It slightly less verbose than `set.contains(node.getElementType())` and overloaded methods with the same name
   * allow check ASTNode/PsiElement against both concrete element types and token sets in uniform way.
   */
  public fun hasElementType(node: ASTNode, set: TokenSet): Boolean {
    return set.contains(node.getElementType())
  }

  /**
   * @see .hasElementType
   */
  public fun hasElementType(node: ASTNode, vararg types: IElementType): Boolean {
    return hasElementType(node, TokenSet.create(*types))
  }

  /**
   * @see .hasElementType
   */
  public fun hasElementType(element: PsiElement, set: TokenSet): Boolean {
    return element.getNode() != null && hasElementType(element.getNode(), set)
  }

  /**
   * @see .hasElementType
   */
  public fun hasElementType(element: PsiElement, vararg types: IElementType): Boolean {
    return element.getNode() != null && hasElementType(element.getNode(), *types)
  }

  /**
   * Returns text of the given PSI element. Unlike obvious [PsiElement.getText] this method unescapes text of the element if latter
   * belongs to injected code fragment using [InjectedLanguageManager.getUnescapedText].

   * @param element PSI element which text is needed
   * *
   * @return text of the element with any host escaping removed
   */
  public fun getElementTextWithoutHostEscaping(element: PsiElement): String {
    val manager = InjectedLanguageManager.getInstance(element.getProject())
    if (manager.isInjectedFragment(element.getContainingFile())) {
      return manager.getUnescapedText(element)
    } else {
      return element.getText()
    }
  }

  /**
   * Returns content of the string literal (without escaping) striving to preserve as much of user data as possible.
   *
   *  * If literal length is greater than one and it starts and ends with the same quote and the last quote is not escaped, returns
   * text without first and last characters.
   *  * Otherwise if literal still begins with a quote, returns text without first character only.
   *  * Returns unmodified text in all other cases.
   *

   * @param text presumably result of [HCLStringLiteral.getText]
   * *
   * @return
   */
  public fun stripQuotes(text: String): String {
    if (text.length() > 0) {
      val firstChar = text.charAt(0)
      val lastChar = text.charAt(text.length() - 1)
      if (firstChar == '\'' || firstChar == '"') {
        if (text.length() > 1 && firstChar == lastChar && !isEscapedChar(text, text.length() - 1)) {
          return text.substring(1, text.length() - 1)
        }
        return text.substring(1)
      }
    }
    return text
  }

  /**
   * Checks that character in given position is escaped with backslashes.

   * @param text     text character belongs to
   * *
   * @param position position of the character
   * *
   * @return whether character at given position is escaped, i.e. preceded by odd number of backslashes
   */
  public fun isEscapedChar(text: String, position: Int): Boolean {
    var count = 0
    var i = position - 1
    while (i >= 0 && text.charAt(i) == '\\') {
      count++
      i--
    }
    return count % 2 != 0
  }
}// empty
