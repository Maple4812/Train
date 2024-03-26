public class Date {
    String REGEXP_PATTERN_SLASH_1 = "[0-9]{2}/[0-9]{2}/[0-9]{2}";   // 24/03/20의 형식
    String REGEXP_PATTERN_SLASH_2 = "[0-9]{4}/[0-9]{2}/[0-9]{2}";   // 2024/03/20의 형식
    String REGEXP_PATTERN_DASH_1 = "[0-9]{2}-[0-9]{2}-[0-9]{2}";    // 24-03-20의 형식
    String REGEXP_PATTERN_DASH_2 = "[0-9]{4}-[0-9]{2}-[0-9]{2}";    // 2024-03-20의 형식
    String REGEXP_PATTERN_NORMAL_1 = "[0-9]{6}";    // 240320의 형식
    String REGEXP_PATTERN_NORMAL_2 = "[0-9]{8}";    // 20240320의 형식
    String NUMBERS_2 = "[0-9]{2}";
    String[] dateArr;

    int year, month, day;
    int firstYear = 2000;   // 최소 년도

    @Override
    public String toString() {
        return year + "년 " +
                month + "월 " +
                day + "일";
    }

    public Date(String input) throws Exception {
        if(input.matches(REGEXP_PATTERN_SLASH_1)){
            dateArr = input.split("/");
            year = Integer.parseInt(dateArr[0]) + 2000;
            month = Integer.parseInt(dateArr[1]);
            day = Integer.parseInt(dateArr[2]);

        } else if (input.matches(REGEXP_PATTERN_SLASH_2)){
            dateArr = input.split("/");
            year = Integer.parseInt(dateArr[0]);
            month = Integer.parseInt(dateArr[1]);
            day = Integer.parseInt(dateArr[2]);

        } else if (input.matches(REGEXP_PATTERN_DASH_1)) {
            dateArr = input.split("-");
            year = Integer.parseInt(dateArr[0]) + 2000;
            month = Integer.parseInt(dateArr[1]);
            day = Integer.parseInt(dateArr[2]);

        } else if (input.matches(REGEXP_PATTERN_DASH_2)) {
            dateArr = input.split("-");
            year = Integer.parseInt(dateArr[0]);
            month = Integer.parseInt(dateArr[1]);
            day = Integer.parseInt(dateArr[2]);

        } else if (input.matches(REGEXP_PATTERN_NORMAL_1)){
            year = Integer.parseInt(input.substring(0, 2)) + 2000;
            month = Integer.parseInt(input.substring(2, 4));
            day = Integer.parseInt(input.substring(4, 6));

        } else if (input.matches(REGEXP_PATTERN_NORMAL_2)){
            year = Integer.parseInt(input.substring(0, 4));
            month = Integer.parseInt(input.substring(4, 6));
            day = Integer.parseInt(input.substring(6, 8));

        }else {
            throw new Exception("잘못된 입력입니다.");
        }
        checkValid(year, month, day);
    }

    private void checkValid(int year, int month, int day) throws Exception {
        if(Integer.toString(year).matches(NUMBERS_2)){
            if(!checkValidYear(year + firstYear))
                throw new Exception("잘못된 연도(Year)입니다.");
        }
        else{
            if(!checkValidYear(year))
                throw new Exception("잘못된 연도(Year)입니다.");
        }
        if(!checkValidMonth(month))
            throw new Exception("잘못된 월(Month)입니다.");
        if(month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12){
            if(!(day >= 1 && day <= 31))
                throw new Exception("잘못된 일(Day)입니다.");
        } else if (month == 4 || month == 6 || month == 9 || month == 11) {
            if(!(day >= 1 && day <= 30))
                throw new Exception("잘못된 일(Day)입니다.");
        } else if (checkLeapYear(year) && month == 2) {
            if(!(day >= 1 && day <= 29))
                throw new Exception("잘못된 일(Day)입니다.");
        } else if (!checkLeapYear(year) && month == 2) {
            if(!(day >= 1 && day <= 28))
                throw new Exception("잘못된 일(Day)입니다.");
        }
    }

    private boolean checkLeapYear(int year){
        if(year%400 == 0)
            return true;
        else if(year%100 == 0)
            return false;
        else if(year%4 == 0)
            return true;
        else
            return false;
    }

    private boolean checkValidYear(int year) {
        return year >= this.firstYear;
    }

    private boolean checkValidMonth(int month) {
        return month >= 1 && month <= 12;
    }
}
