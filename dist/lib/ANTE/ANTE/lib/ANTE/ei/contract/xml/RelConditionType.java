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
 * <p>Java class for relConditionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="relConditionType">
 *   &lt;complexContent>
 *     &lt;extension base="{}situationElementType">
 *       &lt;sequence>
 *         &lt;element name="rel-operator">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="eq"/>
 *               &lt;enumeration value="gt"/>
 *               &lt;enumeration value="ge"/>
 *               &lt;enumeration value="lt"/>
 *               &lt;enumeration value="le"/>
 *               &lt;enumeration value="ne"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="expression" type="{}expressionType" maxOccurs="unbounded" minOccurs="2"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "relConditionType", propOrder = {
    "relOperator",
    "expression"
})
public class RelConditionType
    extends SituationElementType
{

    @XmlElement(name = "rel-operator", required = true)
    protected String relOperator;
    @XmlElement(required = true)
    protected List<ExpressionType> expression;

    /**
     * Gets the value of the relOperator property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRelOperator() {
        return relOperator;
    }

    /**
     * Sets the value of the relOperator property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRelOperator(String value) {
        this.relOperator = value;
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

}