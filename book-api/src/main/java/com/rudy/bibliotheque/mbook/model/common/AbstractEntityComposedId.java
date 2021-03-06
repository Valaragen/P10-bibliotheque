package com.rudy.bibliotheque.mbook.model.common;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import java.io.Serializable;

@Getter
@Setter
@MappedSuperclass
public abstract class AbstractEntityComposedId implements Serializable {
    @Version
    protected Long version;
}
