package com.cfs.pojo.entity;

import java.io.Serializable;
import java.util.Objects;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserOrganisationPK implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long userId;
	private Long organisationId;

	public UserOrganisationPK(Long userId, Long organisationId) {
		this.userId = userId;
		this.organisationId = organisationId;
	}

	public UserOrganisationPK() {
	}

    @Override
    public boolean equals(Object o) {
        if ( this == o ) {
            return true;
        }
        if ( o == null || getClass() != o.getClass() ) {
            return false;
        }
        UserOrganisationPK pk = (UserOrganisationPK) o;
        return Objects.equals( userId, pk.userId ) &&
                Objects.equals( organisationId, pk.organisationId );
    }

    @Override
    public int hashCode() {
        return Objects.hash( userId, organisationId );
    }
}