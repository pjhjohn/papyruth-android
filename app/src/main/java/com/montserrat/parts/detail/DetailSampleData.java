package com.montserrat.parts.detail;

import java.util.Vector;

/**
 * Created by SSS on 2015-04-16.
 */
public class DetailSampleData {
    private Vector<String> searchdata;
    private Vector<lectureData> lecturedata;

    public DetailSampleData(){
        searchdata = new Vector<>();
    }

    public void initSearchData(){
        searchdata.add("가가가");
        searchdata.add("가가나");
        searchdata.add("강아지");
        searchdata.add("강의평가");
        searchdata.add("게잡이");
        searchdata.add("나옹이");
        searchdata.add("나락");
        searchdata.add("냄비");
        searchdata.add("높새바람");
        searchdata.add("다람쥐");
    }

    public void initLectureData(){
        lectureData data;
        for(int i = 0; i < 10; i++) {
            String[] s = {i+"점", i+"점"};
            data = new lectureData("lecture"+i, "prof"+i, s);
            lecturedata.add(data);
        }
    }
    public boolean getSearchData(String s){
        for(String i : searchdata){
            if(i.equals(s)){
                return true;
            }
        }
        return false;
    }

    public class lectureData{
        private String title;
        private String prof;
        private String[] grade = new String[2];
        public lectureData(){

        }
        public lectureData(String title, String prof, String[] gradeList){
            this.title = title;
            this.prof = prof;
            this.grade = gradeList;
        }

        public void setData(String title, String prof, String[] gradeList){
            this.title = title;
            this.prof = prof;
            this.grade = gradeList;
        }
    }
}
