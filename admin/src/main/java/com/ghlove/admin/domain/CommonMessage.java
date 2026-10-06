package com.ghlove.admin.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

/**
 * 운영관리 화면 문구 사전 (AS-IS OP_COMMON_MESSAGE) - AS-IS JSP가 라벨을 전부
 * {@code ${op:message('M00730')}} 코드로 쓰기 때문에, 화면을 AS-IS와 동일하게 만들려면
 * 이 표가 있어야 한다. 2026-10-02 운영DB에서 export 받아 적재했다(ko 2034행 / ja 98행,
 * 고유 M코드 1692개). 문구를 내 해석으로 새로 짓지 않고 여기서 꺼내 쓴다.
 */
@Entity
@Table(name = "OP_COMMON_MESSAGE")
@IdClass(CommonMessage.Key.class)
@Getter
@Setter
@NoArgsConstructor
public class CommonMessage {

    @Id
    @Column(name = "ID")
    private String id;

    /** 'ko' / 'ja' - 운영관리는 AS-IS inc_head가 OP_LANGUAGE='ko' 고정이라 사실상 ko만 쓴다. */
    @Id
    @Column(name = "LANGUAGE")
    private String language;

    @Column(name = "MESSAGE")
    private String message;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Key implements Serializable {
        private String id;
        private String language;

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Key other)) {
                return false;
            }
            return Objects.equals(id, other.id) && Objects.equals(language, other.language);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, language);
        }
    }
}
