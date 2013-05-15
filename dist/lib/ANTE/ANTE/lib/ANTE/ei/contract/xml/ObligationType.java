//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-646 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2008.10.21 at 11:46:12 AM BST 
//


package ei.contract.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for obligationType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="obligationType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="bearer" type="{}varAllowedType"/>
 *         &lt;element name="counterparty" type="{}varAllowedType"/>
 *         &lt;element ref="{}fact"/>
 *         &lt;element name="liveline" type="{}expressionType" minOccurs="0"/>
 *         &lt;element name="deadline" type="{}expressionType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "obligationType", propOrder = {
    "bearer",
    "counterparty",
    "fact",
    "liveline",
    "deadline"
})
public class ObligationType {

    @XmlElement(required = true)
    protected VarAllowedType bearer;
    @XmlElement(required = true)
    protected VarAllowedType counterparty;
    @XmlElement(required = true)
    protected FrameType fact;
    protected ExpressionType liveline;
    @XmlElement(required = true)
    protected ExpressionType deadline;

    /**
     * Gets the value of the bearer property.
     * 
     * @return
     *     possible object is
     *     {@link VarAllowedType }
     *     
     */
    public VarAllowedType getBearer() {
        return bearer;
    }

    /**
     * Sets the value of the bearer property.
     * 
     * @param value
     *     allowed object is
     *     {@link VarAllowedType }
     *     
     */
    public void setBearer(VarAllowedType value) {
        this.bearer = value;
    }

    /**
     * Gets the value of the counterparty property.
     * 
     * @return
     *     possible object is
     *     {@link VarAllowedType }
     *     
     */
    public VarAllowedType getCounterparty() {
        return counterparty;
    }

    /**
     * Sets the value of the counterparty property.
     * 
     * @param value
     *     allowed object is
     *     {@link VarAllowedType }
     *     
     */
    public void setCounterparty(VarAllowedType value) {
        this.counterparty = value;
    }

    /**
     * Gets the value of the fact property.
     * 
     * @return
     *     possible object is
     *     {@link FrameType }
     *     
     */
    public FrameType getFact() {
        return fact;
    }

    /**
     * Sets the value of the fact property.
     * 
     * @param value
     *     allowed object is
     *     {@link FrameType }
     *     
     */
    public void setFact(FrameType value) {
        this.fact = value;
    }

    /**
     * Gets the value of the liveline property.
     * 
     * @return
     *     possible object is
     *     {@link ExpressionType }
     *     
     */
    public ExpressionType getLiveline() {
        return liveline;
    }

    /**
     * Sets the value of the liveline property.
     * 
     * @param value
     *     allowed object is
     *     {@link ExpressionType }
     *     
     */
    public void setLiveline(ExpressionType value) {
        this.liveline = value;
    }

    /**
     * Gets the value of the deadline property.
     * 
     * @return
     *     possible object is
     *     {@link ExpressionType }
     *     
     */
    public ExpressionType getDeadline() {
        return deadline;
    }

    /**
     * Sets the value of the deadline property.
     * 
     * @param value
     *     allowed object is
     *     {@link ExpressionType }
     *     
     */
    public void setDeadline(ExpressionType value) {
        this.deadline = value;
    }

}
