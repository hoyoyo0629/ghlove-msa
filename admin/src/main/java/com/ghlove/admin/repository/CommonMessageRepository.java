package com.ghlove.admin.repository;

import com.ghlove.admin.domain.CommonMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommonMessageRepository extends JpaRepository<CommonMessage, CommonMessage.Key> {

    List<CommonMessage> findByLanguage(String language);

    /** 메뉴관리가 MENU_<menuId> 코드를 지울 때 - @IdClass라 findById는 Key 객체를 받으므로 파생 쿼리를 쓴다. */
    java.util.Optional<CommonMessage> findByIdAndLanguage(String id, String language);

    /* ---------------- 메세지 관리(메뉴 1402) - AS-IS message-mapper.xml 이식 ---------------- */

    /**
     * AS-IS {@code messageMapper.getMessageList} - 한 ID의 ko/ja 문구를 한 행으로 피벗한다
     * (OP_COMMON_MESSAGE를 언어별로 self left join). 결과는 {id, kMessage, jMessage}.
     *
     * AS-IS는 검색어가 비어 있어도 where/query 조합에 따라 LIKE를 걸지만 빈 문자열이면
     * {@code LIKE '%%'}가 되어 전체와 같다 - 그래서 호출부가 빈 검색어를 null로 넘긴다.
     */
    @Query(value = """
            select a.id, ko.message as kmessage, ja.message as jmessage
              from admin.op_common_message a
              left join admin.op_common_message ko on a.id = ko.id and ko.language = 'ko'
              left join admin.op_common_message ja on a.id = ja.id and ja.language = 'ja'
             where (cast(:idKeyword as varchar) is null or a.id like concat('%', :idKeyword, '%'))
               and (cast(:messageKeyword as varchar) is null
                    or ko.message like concat('%', :messageKeyword, '%')
                    or ja.message like concat('%', :messageKeyword, '%'))
             group by a.id, ko.message, ja.message
             order by a.id desc
            """, nativeQuery = true)
    List<Object[]> searchPivot(@Param("idKeyword") String idKeyword,
                               @Param("messageKeyword") String messageKeyword);

    /** AS-IS {@code messageMapper.getMessageById} - 단건 피벗. */
    @Query(value = """
            select a.id, ko.message as kmessage, ja.message as jmessage
              from admin.op_common_message a
              left join admin.op_common_message ko on a.id = ko.id and ko.language = 'ko'
              left join admin.op_common_message ja on a.id = ja.id and ja.language = 'ja'
             where a.id = :id
             group by a.id, ko.message, ja.message
            """, nativeQuery = true)
    List<Object[]> findPivotById(@Param("id") String id);

    /**
     * AS-IS {@code messageMapper.insertMessage2}의 ID 자동채번 -
     * {@code CONCAT('M', SUBSTR(CONCAT('00000', SUBSTR(MAX(ID),2,5) + 1), -5))},
     * 대상은 MENU_로 시작하지 않는 ID들이다(메뉴 문구코드는 채번에서 제외).
     */
    @Query(value = """
            select 'M' || right('00000' || ((cast(substr(max(id), 2, 5) as integer) + 1)::text), 5)
              from admin.op_common_message
             where id not like 'MENU%'
            """, nativeQuery = true)
    String nextMessageId();

    /** AS-IS {@code messageMapper.updatekMessage} / {@code updatejMessage} - 언어별 개별 UPDATE. */
    @Modifying
    @Query("update CommonMessage m set m.message = :message where m.id = :id and m.language = :language")
    int updateMessage(@Param("id") String id, @Param("language") String language,
                      @Param("message") String message);

    /** AS-IS {@code messageMapper.deleteMessage} - ID 하나의 모든 언어 행을 지운다.
     *  (CrudRepository에 {@code deleteAllById(Iterable)}가 있어 이름을 겹치지 않게 뒀다.) */
    @Modifying
    @Query("delete from CommonMessage m where m.id = :id")
    int deleteByMessageId(@Param("id") String id);
}
