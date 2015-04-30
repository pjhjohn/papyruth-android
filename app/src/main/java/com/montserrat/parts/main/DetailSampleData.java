package com.montserrat.parts.main;

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
        for(int i = 0; i < 10; i++) searchdata.add("aaa");
    }

    public void initLectureData(){
        lectureData data;
        for(int i = 0; i < 10; i++) {
            String[] s = {i+"a", i+"b"};
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
