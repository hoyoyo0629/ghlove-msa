package com.ghlove.admin.service;

import com.ghlove.admin.domain.CommonMessage;
import com.ghlove.admin.repository.CommonMessageRepository;
import com.ghlove.admin.web.support.MessageParam;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 메세지 관리 (메뉴 1402) - AS-IS saleson.shop.message.MessageServiceImple 이식.
 *
 * OP_COMMON_MESSAGE(운영관리 화면의 모든 라벨·알럿 문구)를 ID 하나당 ko/ja 한 행으로 피벗해
 * 보여주고 등록·수정·선택삭제한다. AS-IS는 저장할 때마다 프레임워크의 문구 캐시를 reload하므로,
 * TO-BE도 {@link CommonMessageService#reload()}를 같은 자리에서 부른다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CommonMessageAdminService {

    /** AS-IS Message 도메인이 다루는 두 언어 - OP_COMMON_MESSAGE.LANGUAGE. */
    private static final String LANGUAGE_KO = "ko";
    private static final String LANGUAGE_JA = "ja";

    private final CommonMessageRepository commonMessageRepository;
    private final CommonMessageService commonMessageService;

    /** AS-IS messageMapper.getMessageCount - 피벗 후 ID 수. */
    public int getMessageCount(MessageParam param) {
        return search(param).size();
    }

    /** AS-IS messageMapper.getMessageList. */
    public List<MessageRow> getMessageList(MessageParam param) {
        return search(param);
    }

    private List<MessageRow> search(MessageParam param) {
        String keyword = (param.getQuery() == null || param.getQuery().isBlank()) ? null : param.getQuery().trim();
        String idKeyword = "ID".equals(param.getWhere()) ? keyword : null;
        String messageKeyword = "MESSAGE".equals(param.getWhere()) ? keyword : null;
        return commonMessageRepository.searchPivot(idKeyword, messageKeyword).stream()
                .map(MessageRow::of)
                .toList();
    }

    /** AS-IS messageMapper.getMessageById - 수정 화면 진입. */
    public MessageRow getMessageById(String id) {
        return commonMessageRepository.findPivotById(id).stream()
                .findFirst()
                .map(MessageRow::of)
                .orElseThrow(() -> new ManagerException("문구를 찾을 수 없습니다."));
    }

    /**
     * AS-IS insertMessage1 - 화면에서 ID를 직접 입력한 경우(메뉴 문구 MENU_xxxx 등).
     * ko/ja 두 행을 함께 넣는다.
     */
    @Transactional
    public void insertMessage1(String id, String kMessage, String jMessage) {
        save(id, LANGUAGE_KO, kMessage);
        save(id, LANGUAGE_JA, jMessage);
        commonMessageService.reload();
    }

    /**
     * AS-IS insertMessage2 - ID를 비워 둔 경우 자동채번(M + 5자리).
     * AS-IS는 INSERT 안에서 MAX(ID)+1을 계산하고 문구를 trim한다.
     */
    @Transactional
    public String insertMessage2(String kMessage, String jMessage) {
        String id = commonMessageRepository.nextMessageId();
        if (id == null || id.isBlank()) {
            throw new ManagerException("문구 ID를 채번할 수 없습니다.");
        }
        save(id, LANGUAGE_KO, trim(kMessage));
        save(id, LANGUAGE_JA, trim(jMessage));
        commonMessageService.reload();
        return id;
    }

    /**
     * AS-IS updatekMessage + updatejMessage - 언어별로 따로 UPDATE한다.
     *
     * AS-IS 결함 보존: 해당 언어 행이 없으면 UPDATE가 0건이라 입력값이 조용히 사라진다.
     * 일본어 행은 전체 2,034개 ID 중 98개에만 있어서, 나머지 ID를 수정하면 화면의 일본어 칸에
     * 적은 값은 저장되지 않는다(등록 폼 JS가 비어 있으면 한국어를 복사해 넣어주므로 항상 값이
     * 실려 온다). INSERT로 보충하면 AS-IS에 없던 ja 행이 수정할 때마다 생기므로 그대로 둔다.
     */
    @Transactional
    public void updateMessage(String id, String kMessage, String jMessage) {
        commonMessageRepository.updateMessage(id, LANGUAGE_KO, kMessage);
        commonMessageRepository.updateMessage(id, LANGUAGE_JA, jMessage);
        commonMessageService.reload();
    }

    /** AS-IS deleteMessageData - 체크한 ID들의 모든 언어 행을 지운다. */
    @Transactional
    public void deleteMessageData(List<String> ids) {
        if (ids == null) {
            return;
        }
        for (String id : ids) {
            if (id != null && !id.isBlank()) {
                commonMessageRepository.deleteByMessageId(id);
            }
        }
        commonMessageService.reload();
    }

    private void save(String id, String language, String message) {
        CommonMessage entity = new CommonMessage();
        entity.setId(id);
        entity.setLanguage(language);
        entity.setMessage(message);
        commonMessageRepository.save(entity);
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }

    /** AS-IS saleson.shop.message.domain.Message - ID 하나의 ko/ja 문구. */
    @Getter
    public static class MessageRow {
        private final String id;
        private final String kMessage;
        private final String jMessage;

        public MessageRow(String id, String kMessage, String jMessage) {
            this.id = id;
            this.kMessage = kMessage;
            this.jMessage = jMessage;
        }

        static MessageRow of(Object[] row) {
            return new MessageRow((String) row[0], (String) row[1], (String) row[2]);
        }
    }
}
