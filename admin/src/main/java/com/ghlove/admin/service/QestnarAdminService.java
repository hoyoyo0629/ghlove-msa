package com.ghlove.admin.service;

import com.ghlove.admin.repository.QestnarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 설문관리 (AS-IS saleson.shop.qustnr.QustnrService + qustnr-mapper.xml).
 * 등록/수정 화면이 설문·문항·선택지를 한 덩어리 JSON으로 보내므로, 저장은 AS-IS와 같은 순서로
 * 처리한다: 설문 upsert → 이번에 안 온 문항 제거 → 문항별 upsert → 그 문항에서 안 온 선택지 제거
 * → 선택지 upsert. 제거는 응답결과(G_QUSTNR_RSPNS_RESULT)부터 지워 참조를 끊는다.
 */
@Service
@RequiredArgsConstructor
public class QestnarAdminService {

    /** 문항 유형 - AS-IS form.jsp가 보내는 값 그대로. */
    public static final String TYPE_CHOICE = "rtype";       // 객관식
    public static final String TYPE_SUBJECTIVE = "stype";   // 주관식

    private final QestnarRepository qestnarRepository;

    /** 화면이 보내는 선택지 한 건. */
    public record IemForm(Integer qustnrIemSn, String iemCn) {
    }

    /** 화면이 보내는 문항 한 건. */
    public record QesitmForm(Integer qustnrQesitmSn, String qestnCn, String qestnTyCode,
                             Integer parentSn, List<IemForm> qustnrIem) {
    }

    /** 화면이 보내는 설문 전체(JSON 본문). */
    public record QestnarForm(String qustnrSj, String qustnrBgnDe, String qustnrEndDe, String srvyTrgt,
                              List<QesitmForm> qustnrQesitm) {
    }

    /** 상세화면이 그릴 문항 한 건(선택지 포함). */
    public record QesitmView(Integer qustnrQesitmSn, Integer qestnSn, String qestnTyCode, String qestnCn,
                             Integer parentSn, List<IemView> qustnrIem) {
    }

    public record IemView(Integer qustnrIemSn, Integer iemSn, String iemCn, long userCnt) {
    }

    public int count(String searchTxt) {
        return qestnarRepository.getQustnrListCnt(blankToNull(searchTxt));
    }

    public List<QestnarRepository.QestnarRow> list(String searchTxt) {
        return qestnarRepository.getQustnrList(blankToNull(searchTxt));
    }

    public QestnarRepository.QestnarRow get(long qustnrSn) {
        return qestnarRepository.getQustnr(qustnrSn);
    }

    public long responseCount(long qustnrSn) {
        return qestnarRepository.getQustnrRspnsResultCnt(qustnrSn);
    }

    /** 문항+선택지를 화면이 쓰기 좋게 묶는다 - 평면 조회 결과를 문항 단위로 접는다. */
    public List<QesitmView> qesitmViews(long qustnrSn) {
        Map<Integer, QesitmView> byQesitm = new LinkedHashMap<>();
        Map<Integer, List<IemView>> iemsByQesitm = new LinkedHashMap<>();
        Map<Integer, Integer> qestnSnByQesitm = new LinkedHashMap<>();
        Map<Integer, String[]> metaByQesitm = new LinkedHashMap<>();
        Map<Integer, Integer> parentByQesitm = new LinkedHashMap<>();

        for (QestnarRepository.QesitmDetailRow row : qestnarRepository.getQustnrQesitmDetail(qustnrSn)) {
            iemsByQesitm.computeIfAbsent(row.qustnrQesitmSn(), k -> new ArrayList<>());
            qestnSnByQesitm.putIfAbsent(row.qustnrQesitmSn(), row.qestnSn());
            metaByQesitm.putIfAbsent(row.qustnrQesitmSn(), new String[] { row.qestnTyCode(), row.qestnCn() });
            parentByQesitm.putIfAbsent(row.qustnrQesitmSn(), row.parentSn());
            if (row.qustnrIemSn() != null) {
                iemsByQesitm.get(row.qustnrQesitmSn())
                        .add(new IemView(row.qustnrIemSn(), row.iemSn(), row.iemCn(), row.userCnt()));
            }
        }
        for (Integer qesitmSn : iemsByQesitm.keySet()) {
            String[] meta = metaByQesitm.get(qesitmSn);
            byQesitm.put(qesitmSn, new QesitmView(qesitmSn, qestnSnByQesitm.get(qesitmSn),
                    meta[0], meta[1], parentByQesitm.get(qesitmSn), iemsByQesitm.get(qesitmSn)));
        }
        return new ArrayList<>(byQesitm.values());
    }

    /** AS-IS create - 설문을 새로 넣고 문항·선택지를 이어 넣는다. */
    @Transactional
    public long create(QestnarForm form, Long managerId) {
        long qustnrSn = qestnarRepository.insertQustnr(form.qustnrSj(), null,
                form.qustnrBgnDe(), form.qustnrEndDe(), form.srvyTrgt(), managerId);
        saveQesitms(qustnrSn, form, managerId);
        return qustnrSn;
    }

    /** AS-IS edit - 설문을 갱신하고 문항·선택지를 upsert하며 빠진 것은 지운다. */
    @Transactional
    public void update(long qustnrSn, QestnarForm form, Long managerId) {
        qestnarRepository.updateQustnr(qustnrSn, form.qustnrSj(),
                form.qustnrBgnDe(), form.qustnrEndDe(), form.srvyTrgt(), managerId);
        saveQesitms(qustnrSn, form, managerId);
    }

    private void saveQesitms(long qustnrSn, QestnarForm form, Long managerId) {
        List<QesitmForm> qesitms = form.qustnrQesitm() == null ? List.of() : form.qustnrQesitm();

        List<Integer> keep = qesitms.stream()
                .map(QesitmForm::qustnrQesitmSn)
                .filter(sn -> sn != null && sn > 0)
                .toList();
        qestnarRepository.deleteQesitmNotIn(qustnrSn, keep);

        for (QesitmForm qesitm : qesitms) {
            Integer qesitmSn = qesitm.qustnrQesitmSn();
            if (qesitmSn == null || qesitmSn <= 0) {
                continue;   // 화면이 1..n으로 번호를 매겨 보내므로 여기 걸리면 비정상 입력이다
            }
            List<IemForm> iems = qesitm.qustnrIem() == null ? List.of() : qesitm.qustnrIem();

            if (qestnarRepository.existsQesitm(qustnrSn, qesitmSn)) {
                qestnarRepository.updateQustnrQesitm(qustnrSn, qesitmSn, qesitm.qestnCn(), managerId);
            } else {
                qestnarRepository.insertQustnrQesitm(qustnrSn, qesitmSn, qesitm.qestnTyCode(),
                        qesitm.qestnCn(), iems.size(), normalizeParent(qesitm.parentSn()), managerId);
            }

            List<Integer> keepIems = iems.stream()
                    .map(IemForm::qustnrIemSn)
                    .filter(sn -> sn != null && sn > 0)
                    .toList();
            qestnarRepository.deleteIemNotIn(qustnrSn, qesitmSn, keepIems);

            for (IemForm iem : iems) {
                if (iem.qustnrIemSn() != null && iem.qustnrIemSn() > 0
                        && qestnarRepository.existsIem(qustnrSn, qesitmSn, iem.qustnrIemSn())) {
                    qestnarRepository.updateQustnrIem(qustnrSn, qesitmSn, iem.qustnrIemSn(), iem.iemCn(), managerId);
                } else {
                    qestnarRepository.insertQustnrIem(qustnrSn, qesitmSn, iem.iemCn(), managerId);
                }
            }
        }
    }

    /** AS-IS deleteQustrn - 목록 선택삭제. */
    @Transactional
    public void delete(List<Long> qustnrSns) {
        if (qustnrSns == null) {
            return;
        }
        qustnrSns.forEach(qestnarRepository::deleteQustnr);
    }

    /** 화면은 최상위 문항의 parentSn을 '0'으로 보낸다(연계질문은 부모 문항번호). */
    private static Integer normalizeParent(Integer parentSn) {
        return parentSn == null ? 0 : parentSn;
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
