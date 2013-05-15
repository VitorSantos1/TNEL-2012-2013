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
 * <p>Java class for normType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="normType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="comment" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="scope" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="contract-type" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="id" type="{}varAllowedType"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="situation">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="situation-element" type="{}situationElementType" maxOccurs="unbounded"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="prescription">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;choice>
 *                   &lt;element name="obligation" type="{}obligationType" maxOccurs="unbounded" minOccurs="0"/>
 *                   &lt;element name="end-contract" type="{}endContractType"/>
 *                 &lt;/choice>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "normType", propOrder = {
    "name",
    "comment",
    "scope",
    "situation",
    "prescription"
})
public class NormType {

    @XmlElement(required = true)
    protected String name;
    protected String comment;
    protected NormType.Scope scope;
    @XmlElement(required = true)
    protected NormType.Situation situation;
    @XmlElement(required = true)
    protected NormType.Prescription prescription;

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the comment property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets the value of the comment property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setComment(String value) {
        this.comment = value;
    }

    /**
     * Gets the value of the scope property.
     * 
     * @return
     *     possible object is
     *     {@link NormType.Scope }
     *     
     */
    public NormType.Scope getScope() {
        return scope;
    }

    /**
     * Sets the value of the scope property.
     * 
     * @param value
     *     allowed object is
     *     {@link NormType.Scope }
     *     
     */
    public void setScope(NormType.Scope value) {
        this.scope = value;
    }

    /**
     * Gets the value of the situation property.
     * 
     * @return
     *     possible object is
     *     {@link NormType.Situation }
     *     
     */
    public NormType.Situation getSituation() {
        return situation;
    }

    /**
     * Sets the value of the situation property.
     * 
     * @param value
     *     allowed object is
     *     {@link NormType.Situation }
     *     
     */
    public void setSituation(NormType.Situation value) {
        this.situation = value;
    }

    /**
     * Gets the value of the prescription property.
     * 
     * @return
     *     possible object is
     *     {@link NormType.Prescription }
     *     
     */
    public NormType.Prescription getPrescription() {
        return prescription;
    }

    /**
     * Sets the value of the prescription property.
     * 
     * @param value
     *     allowed object is
     *     {@link NormType.Prescription }
     *     
     */
    public void setPrescription(NormType.Prescription value) {
        this.prescription = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;choice>
     *         &lt;element name="obligation" type="{}obligationType" maxOccurs="unbounded" minOccurs="0"/>
     *         &lt;element name="end-contract" type="{}endContractType"/>
     *       &lt;/choice>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "obligation",
        "endContract"
    })
    public static class Prescription {

        protected List<ObligationType> obligation;
        @XmlElement(name = "end-contract")
        protected EndContractType endContract;

        /**
         * Gets the value of the obligation property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the obligation property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getObligation().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link ObligationType }
         * 
         * 
         */
        public List<ObligationType> getObligation() {
            if (obligation == null) {
                obligation = new ArrayList<ObligationType>();
            }
            return this.obligation;
        }

        /**
         * Gets the value of the endContract property.
         * 
         * @return
         *     possible object is
         *     {@link EndContractType }
         *     
         */
        public EndContractType getEndContract() {
            return endContract;
        }

        /**
         * Sets the value of the endContract property.
         * 
         * @param value
         *     allowed object is
         *     {@link EndContractType }
         *     
         */
        public void setEndContract(EndContractType value) {
            this.endContract = value;
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="contract-type" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="id" type="{}varAllowedType"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "contractType",
        "id"
    })
    public static class Scope {

        @XmlElement(name = "contract-type")
        protected String contractType;
        @XmlElement(required = true)
        protected VarAllowedType id;

        /**
         * Gets the value of the contractType property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getContractType() {
            return contractType;
        }

        /**
         * Sets the value of the contractType property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setContractType(String value) {
            this.contractType = value;
        }

        /**
         * Gets the value of the id property.
         * 
         * @return
         *     possible object is
         *     {@link VarAllowedType }
         *     
         */
        public VarAllowedType getId() {
            return id;
        }

        /**
         * Sets the value of the id property.
         * 
         * @param value
         *     allowed object is
         *     {@link VarAllowedType }
         *     
         */
        public void setId(VarAllowedType value) {
            this.id = value;
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="situation-element" type="{}situationElementType" maxOccurs="unbounded"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "situationElement"
    })
    public static class Situation {

        @XmlElement(name = "situation-element", required = true)
        protected List<SituationElementType> situationElement;

        /**
         * Gets the value of the situationElement property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the situationElement property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getSituationElement().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link SituationElementType }
         * 
         * 
         */
        public List<SituationElementType> getSituationElement() {
            if (situationElement == null) {
                situationElement = new ArrayList<SituationElementType>();
            }
            return this.situationElement;
        }

    }

}