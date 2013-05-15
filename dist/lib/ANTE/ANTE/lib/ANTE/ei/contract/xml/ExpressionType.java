//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-646 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2008.10.21 at 11:46:12 AM BST 
//


package ei.contract.xml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for expressionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="expressionType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;sequence>
 *           &lt;element name="num-operator">
 *             &lt;simpleType>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                 &lt;enumeration value="+"/>
 *                 &lt;enumeration value="-"/>
 *                 &lt;enumeration value="*"/>
 *                 &lt;enumeration value="/"/>
 *               &lt;/restriction>
 *             &lt;/simpleType>
 *           &lt;/element>
 *           &lt;element name="expression" type="{}expressionType" maxOccurs="unbounded" minOccurs="2"/>
 *         &lt;/sequence>
 *         &lt;element name="operand" type="{}varAllowedType"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "expressionType", propOrder = {
    "numOperator",
    "expression",
    "operand"
})
public class ExpressionType {

    @XmlElement(name = "num-operator")
    protected String numOperator;
    protected List<ExpressionType> expression;
    protected VarAllowedType operand;

    /**
     * Gets the value of the numOperator property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNumOperator() {
        return numOperator;
    }

    /**
     * Sets the value of the numOperator property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNumOperator(String value) {
        this.numOperator = value;
    }

    /**
     * Gets the value of the expression property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the expression property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getExpression().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ExpressionType }
     * 
     * 
     */
    public List<ExpressionType> getExpression() {
        if (expression == null) {
            expression = new ArrayList<ExpressionType>();
        }
        return this.expression;
    }

    /**
     * Gets the value of the operand property.
     * 
     * @return
     *     possible object is
     *     {@link VarAllowedType }
     *     
     */
    public VarAllowedType getOperand() {
        return operand;
    }

    /**
     * Sets the value of the operand property.
     * 
     * @param value
     *     allowed object is
     *     {@link VarAllowedType }
     *     
     */
    public void setOperand(VarAllowedType value) {
        this.operand = value;
    }

}
