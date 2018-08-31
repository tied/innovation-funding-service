package org.innovateuk.ifs.eugrant.domain;

import org.innovateuk.ifs.eugrant.EuOrganisationType;

import javax.persistence.*;

import static javax.persistence.EnumType.STRING;

/**
 * A UK Organisation that receives EU grant funding.
 */
@Entity
public class EuOrganisation {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    @OneToOne
    private EuAddress address;
    @Enumerated(STRING)
    private EuOrganisationType organisationType;
    private String companiesHouseNumber;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EuAddress getAddress() {
        return address;
    }

    public void setAddress(EuAddress address) {
        this.address = address;
    }

    public EuOrganisationType getOrganisationType() {
        return organisationType;
    }

    public void setOrganisationType(EuOrganisationType organisationType) {
        this.organisationType = organisationType;
    }

    public String getCompaniesHouseNumber() {
        return companiesHouseNumber;
    }

    public void setCompaniesHouseNumber(String companiesHouseNumber) {
        this.companiesHouseNumber = companiesHouseNumber;
    }
}