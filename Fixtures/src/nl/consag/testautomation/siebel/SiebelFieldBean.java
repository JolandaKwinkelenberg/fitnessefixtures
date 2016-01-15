/**
 * Siebel Repository object type: Field
 * @author Jac. Beekers
 * @version 1.1.0.0 26 January 2015
 */
package nl.consag.testautomation.siebel;

import nl.consag.supporting.Constants;

public class SiebelFieldBean {

        private String name;
        private String pickListName;
        private String hasPickList;
        private String joinName;
        private String isReadOnly;
        private String isJoinedThroughPickList;
        private String isMultiValued;
        private String hasLinkSpecification;
        private String linkSpecification;
        private String mvlBusCompName;
        private String multiValueLink;
        private String pickListJoinName;
        private String type;

    /**
     * @return
     */
    public String getName() {
            return name;
        }
        protected void setName(String name) {
            this.name = name;
        }

    /**
     * @return
     */
    public String getPickListName() {
            return pickListName;
        }
        protected void setPickListName(String pickListName) {
            this.pickListName = pickListName;
        }

    /**
     * @return
     */
    public String getHasPickList() {
            return hasPickList;
        }
        protected void setHasPickList(String hasPickList) {
            this.hasPickList =hasPickList;
        }

    /**
     * @return
     */
    public String getJoinName() {
            return joinName;
        }
        protected void setJoinName(String joinName) {
            this.joinName = joinName;
        }

    /**
     * @return
     */
    public String getIsReadOnly() {
            return isReadOnly;
        }
        protected void setIsReadOnly(String isReadOnly) {
            this.isReadOnly = isReadOnly;
        }

    /**
     * @return
     */
    public String getIsJoinedThroughPickList() {
            return isJoinedThroughPickList;
        }
        protected void setIsJoinedThroughPickList(String isJoinedThroughPickList) {
            this.isJoinedThroughPickList = isJoinedThroughPickList;
        }

    /**
     * @return
     */
    public String getIsMultiValued() {
            return isMultiValued;
        }
        protected void setIsMultiValued(String isMultiValued) {
            if(isMultiValued.equalsIgnoreCase(Constants.Y)) {
                this.isMultiValued = Constants.YES;
            } else {
                if(isMultiValued.equalsIgnoreCase(Constants.N)) {
                    this.isMultiValued = Constants.NO;
                } else {
                    this.isMultiValued = isMultiValued;                    
                }
            }
        }

    /**
     * @return
     */
    public String getPickListJoinName() {
            return pickListJoinName;
        }
        protected void setPickListJoinName(String pickListJoinName) {
            this.pickListJoinName = pickListJoinName;
        }

    /**
     * @return
     */
    public String getType() {
            return type;
        }
        protected void setType(String type) {
            this.type = type;
        }

    /**
     * @return
     */
    public String getLinkSpecification() {
        return linkSpecification;
    }
    protected void setLinkSpecification(String linkSpec) {
        this.linkSpecification = linkSpec;
    }

    /**
     * @return
     */
    public String getMvlBusCompName() {
        return mvlBusCompName;
    }
    protected void setMvlBusCompName(String bcName) {
        this.mvlBusCompName = bcName;
    }

    /**
     * @return
     */
    public String getMultiValueLink() {
        return multiValueLink;
    }
    protected void setMultiValueLink(String mvl) {
                this.multiValueLink = mvl;
    }


     

}
