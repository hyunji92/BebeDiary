package com.hyundeee.app.wingsui.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import com.bebediary.R
import kotlinx.android.synthetic.main.check_list_header.view.*
import kotlinx.android.synthetic.main.check_list_item.view.*


class CheckListAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {

    lateinit var onItemClickListener: AdapterView.OnItemClickListener

    var earlyCheckList = listOf<String>(
        "임신 확인서/진단서 발급",
        "국민행복카드 발급 / 바우처 신청 (병원비 등 국가혜택)",
        "보건소 무료 검사 및 영양제 등 혜택",
        "산후돌보미(정부지원) / 산후조리원 / 태아보험 알아보기",
        "산전검사",
        "1차기형아검사"
    )
    var midCheckList = listOf<String>("2차기형아검사", "정밀 초음파")

    //ArrayList<String>()


    /* "임신 확인서/진단서 발급",
     "국민행복카드 발급 / 바우처 신청 (병원비 등 국가혜택)",
     "보건소 무료 검사 및 영양제 등 혜택",
     "산후돌보미(정부지원) / 산후조리원 / 태아보험 알아보기",
     "산전검사",
     "1차기형아검사"
*/

    /*
    * -초기
임신 확인서/진단서 발급
국민행복카드 발급 / 바우처 신청 (병원비 등 국가혜택)
보건소 무료 검사 및 영양제 등 혜택
산후돌보미(정부지원) / 산후조리원 / 태아보험 알아보기
산전검사
1차기형아검사

-중기
2차기형아검사
정밀 초음파

-후기
입체 초음파
출산가방 싸기 (자세히 보기)


-출산 후
산후도우미 / 산후조리원에 출산 알리기
출생신고 및 국가 지원 수당 신청
태아보험 등재
아이사랑포털 - 아기 등록 및 어린이집 입소대기 신청
예방접종] BCG
예방접종] B형 간염
예방접종] 폐렴구균 / 디프테리아 / 파상풍 / 백일해 / 폴리오 / b형 헤모필루스 인플루엔자
예방접종] 홍역 / 유행성이하선염 / 풍진 / 수두 / A형 간염 / 일본뇌염 / 인플루엔자
예방접종] 선택 사항 : 로타바이러스 / 수막구균 / 장티푸스 등
    * */

    companion object {
        val EARLY_HEADER = 0
        val EARLY_PREGNANCY = 1
        val MID_HEADER = 2
        val MID_PREGNANCY = 3
        val LAST_HEADER = 4
        val LAST_PREGNANCY = 5
        val AFTER_HEADER = 6
        val AFTER_CHILDBIRTH = 7

    }

    override fun getItemViewType(position: Int): Int {

        return when (position) {
            0 -> EARLY_HEADER
            earlyCheckList.size+1 -> MID_HEADER
            else -> EARLY_PREGNANCY
        }
    }

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {

        if (holder is EarlyPregnantHeaderViewHolder) {
            holder.run {
                bindView("임신초기")
            }
        } else if (holder is MidPregnantHeaderViewHolder) {
            holder.run {
                bindView("임신중기")
            }
        } else if (holder is EarlyPregnantViewHolder) {
            holder.run {
                bindView(earlyCheckList[position - 1])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        when (viewType) {
            EARLY_HEADER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.check_list_header, parent, false)
                return EarlyPregnantHeaderViewHolder(view)
            }
            EARLY_PREGNANCY -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.check_list_item, parent, false)
                return EarlyPregnantViewHolder(view)
            }
            MID_HEADER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.check_list_header, parent, false)
                return MidPregnantHeaderViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.check_list_item, parent, false)
                return EarlyPregnantViewHolder(view)
            }
        }
    }

    override fun getItemCount(): Int = earlyCheckList.size + 2

    inner class EarlyPregnantHeaderViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        fun bindView(title: String) {
            with(itemView) {
                pregnant_period.text = title
            }
        }
    }
    inner class MidPregnantHeaderViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        fun bindView(title: String) {
            with(itemView) {
                pregnant_period.text = title
            }
        }
    }

    inner class EarlyPregnantViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        fun bindView(earlyChecklistItem: String) {
            with(itemView) {
                check_list_text.text = earlyChecklistItem
            }
        }
    }


}
