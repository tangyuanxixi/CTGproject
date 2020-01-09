package com.lianmed.analyse;

public class Assessment {

	public boolean inRange(int mynum, int min, int max) {
		boolean result = false;

		if (mynum >= min && mynum <= max)
			result = true;

		return result;
	}
	public boolean inRangef(float mynum, int min, int max) {
		boolean result = false;

		if (mynum >= min && mynum <= max)
			result = true;

		return result;
	}
	

	public KerbsRst KerbsAssessment(AnalyseResult analyseResult) {

		int fhrbaselinevalue = 0;
		int zhenfu_tv = 0;
		int zhouqi_tv = 0;
		float accnum = 0;
		float decnum = 0;
		float fmnum = 0;
		
		boolean zaojian = false;
		if (analyseResult.ed>0){
			
			zaojian = true;
		}
		
		fhrbaselinevalue = analyseResult.fhrbaselinev;
		zhenfu_tv = analyseResult.zv;
		zhouqi_tv = analyseResult.qv;
		
		accnum = analyseResult.getAccHalfHour();
		decnum = analyseResult.getDecHalfHour();
		fmnum = analyseResult.getFmHalfHour();
		
		KerbsRst kerbsRst = new KerbsRst();

		// 基线
		if (fhrbaselinevalue < 100 || fhrbaselinevalue > 180) {
			kerbsRst.fhrbaseline_score = 0;
		} else if (inRange(fhrbaselinevalue, 100, 119) || inRange(fhrbaselinevalue, 161, 180)) {
			kerbsRst.fhrbaseline_score = 1;
		} else if (inRange(fhrbaselinevalue, 120, 160)) {
			kerbsRst.fhrbaseline_score = 2;
		}

		// 振幅变异
		if (zhenfu_tv < 5) {
			kerbsRst.zhenfu_lv_score = 0;
		} else if (inRange(zhenfu_tv, 5, 9) || zhenfu_tv > 25) {
			kerbsRst.zhenfu_lv_score = 1;
		} else if (inRange(zhenfu_tv, 10, 25)) {
			kerbsRst.zhenfu_lv_score = 2;
		}

		// 周期变异
		if (zhouqi_tv < 3) {
			kerbsRst.zhouqi_lv_score = 0;
		} else if (inRange(zhouqi_tv, 3, 6)) {
			kerbsRst.zhouqi_lv_score = 1;
		} else if (zhouqi_tv > 6) {
			kerbsRst.zhouqi_lv_score = 2;
		}
		// 加速
		if (accnum == 0) {
			kerbsRst.acc_score = 0;
		} else if (inRangef(accnum, 1, 4)) {
			kerbsRst.acc_score = 1;
		} else if (accnum > 4) {
			kerbsRst.acc_score = 2;
		}

		// 减速
		if (decnum >= 2) {
			kerbsRst.dec_score = 0;
		} else if (decnum == 1) {
			kerbsRst.dec_score = 1;
		} else if (decnum == 1 || zaojian) {
			kerbsRst.dec_score = 2;
		}

		// 胎动
		if (fmnum == 0) {
			kerbsRst.movement_score = 0;
		} else if (inRangef(fmnum, 1, 4)) {
			kerbsRst.movement_score = 1;
		} else if (fmnum > 4) {
			kerbsRst.movement_score = 2;
		}
		return kerbsRst;
	}

	public FisherRst FisherAssessment(AnalyseResult analyseResult) {

		int fhrbaselinevalue = 0;
		int zhenfu_tv = 0;
		int zhouqi_tv = 0;
		float accnum = 0;
		float decnum = 0;
		
		
		boolean chifadec = false;
		boolean bianyidec = false;
		
		int fmnum = 0;
//		boolean zaojian = false;
		
		fhrbaselinevalue = analyseResult.fhrbaselinev;
		zhenfu_tv = analyseResult.zv;
		zhouqi_tv = analyseResult.qv;
		accnum = analyseResult.getAccHalfHour();
		decnum = analyseResult.getDecHalfHour();
		
		if (analyseResult.ld>0){
			chifadec = true;
		}
		if(analyseResult.vd>0){
			bianyidec = true;
		}
		
		
		
		FisherRst fisherRst = new FisherRst();
		// KerbsRst kerbsRst = new KerbsRst();

		// 基线
		if (fhrbaselinevalue < 100 || fhrbaselinevalue > 180) {
			fisherRst.fhrbaseline_score = 0;
		} else if (inRange(fhrbaselinevalue, 100, 119) || inRange(fhrbaselinevalue, 161, 180)) {
			fisherRst.fhrbaseline_score = 1;
		} else if (inRange(fhrbaselinevalue, 120, 160)) {
			fisherRst.fhrbaseline_score = 2;
		}

		// 振幅变异
		if (zhenfu_tv < 5) {
			fisherRst.zhenfu_lv_score = 0;
		} else if (inRange(zhenfu_tv, 5, 9) || zhenfu_tv > 25) {
			fisherRst.zhenfu_lv_score = 1;
		} else if (inRange(zhenfu_tv, 10, 25)) {
			fisherRst.zhenfu_lv_score = 2;
		}

		// 周期变异
		if (zhouqi_tv < 3) {
			fisherRst.zhouqi_lv_score = 0;
		} else if (inRange(zhouqi_tv, 3, 6)) {
			fisherRst.zhouqi_lv_score = 1;
		} else if (zhouqi_tv > 6) {
			fisherRst.zhouqi_lv_score = 2;
		}
		// 加速
		if (accnum == 0) {
			fisherRst.acc_score = 0;
		} else if (inRangef(accnum, 1, 4)) {
			fisherRst.acc_score = 1;
		} else if (accnum > 4) {
			fisherRst.acc_score = 2;
		}
		// 减速
		if (chifadec) {
			fisherRst.dec_score = 0;
		} else if (bianyidec) {
			fisherRst.dec_score = 1;
		} else{
			fisherRst.dec_score = 2;
		}
		return fisherRst;
	}

	
	public NstRst NstAssessment(AnalyseResult analyseResult) {

		int fhrbaselinevalue = 0;
		int zhenfu_tv = 0;
		int zhouqi_tv = 0;
		
		float fhr_uptime = 0;//胎动FHR上升时间
		float fm_fhrv = 0;//胎动FHR变化幅度
		
		
		
		float accnum = 0;
		float decnum = 0;
		
		boolean chifadec = false;
		boolean bianyidec = false;
		
		int fmnum = 0;
		boolean zaojian = false;

		fhrbaselinevalue = analyseResult.fhrbaselinev;
		zhenfu_tv = analyseResult.zv;
		zhouqi_tv = analyseResult.qv;
		accnum = analyseResult.getAccHalfHour();
		decnum = analyseResult.getDecHalfHour();
		
		fhr_uptime = analyseResult.getFmFHRuptime();
		fm_fhrv = analyseResult.getFmFHRupDif();
		
		if (analyseResult.ld>0){
			chifadec = true;
		}
		if(analyseResult.vd>0){
			bianyidec = true;
		}
		
		
		
		NstRst nstRst = new NstRst();
		// KerbsRst kerbsRst = new KerbsRst();

		// 基线
		if (fhrbaselinevalue < 100 ) {
			nstRst.fhrbaseline_score = 0;
		} else if (inRange(fhrbaselinevalue, 100, 119) || fhrbaselinevalue>160) {
			nstRst.fhrbaseline_score = 1;
		} else if (inRange(fhrbaselinevalue, 120, 160)) {
			nstRst.fhrbaseline_score = 2;
		}

		// 振幅变异
		if (zhenfu_tv < 5) {
			nstRst.zhenfu_lv_score = 0;
		} else if (inRange(zhenfu_tv, 5, 9) || zhenfu_tv > 30) {
			nstRst.zhenfu_lv_score = 1;
		} else if (inRange(zhenfu_tv, 10, 30)) {
			nstRst.zhenfu_lv_score = 2;
		}

		// 胎动fhr上升时间
		if (fhr_uptime < 10) {
			nstRst.fhr_uptime_score = 0;
		} else if (inRangef(fhr_uptime, 10, 14)) {
			nstRst.fhr_uptime_score = 1;
		} else if (fhr_uptime > 15) {
			nstRst.fhr_uptime_score = 2;
		}
		// 胎动fhr变化幅度
		if (fm_fhrv < 10) {
			nstRst.fm_fhrv_score = 0;
		} else if (inRangef(fm_fhrv, 10, 14)) {
			nstRst.fm_fhrv_score = 1;
		} else if (fm_fhrv > 15) {
			nstRst.fm_fhrv_score = 2;
		}
		// 减速
		if (fmnum==0) {
			nstRst.fm_score = 0;
		} else if (inRangef(fmnum, 1, 2)) {
			nstRst.fm_score = 1;
		} else if(fmnum>2){
			nstRst.fm_score = 2;
		}
		return nstRst;
	}
	
	
	
	
}
