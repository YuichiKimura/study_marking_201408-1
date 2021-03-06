package jp.ktsystem.kadai201408.e_yamaguchi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;

/**
 * <h3>課題クラス</h3>
 *
 * @author e_yamaguchi
 * @since 2014/08/10
 */
public class Kadai {

	/**
	 * 課題1
	 * <br>
	 * ファイルを読み込み、データの点数の積の合計を計算する。
	 * @param anInputPath 入力ファイルパス
	 * @return データの点数の積の合計
	 * @throws KadaiException
	 */
	public static long calcScoreSum(String anInputPath) throws KadaiException {

		// 入力ファイルnull・空文字チェック
		if (checkFile(anInputPath)) {
			throw new KadaiException(KadaiConstants.FILE_INPUT_OUTPUT_ERROR);
		}

		// ファイルを読み込み、データの点数の積を取得
		Map<Integer, ScoreInfoBean> workTimeMap = readScoreFile(anInputPath);

		// 点数の積の合計
		long scoreSum = 0;

		for (Entry<Integer, ScoreInfoBean> key : workTimeMap.entrySet()) {
			// 点数の積を加算
			scoreSum += key.getValue().getScoreMultiply();
		}

		return scoreSum;
	}

	/**
	 * 課題2
	 * <br>
	 * ファイルを読み込み、データの点数の最高点を取得し、
	 * ファイルに出力する。
	 * @param anInputPath  入力ファイルパス
	 * @param anOutputPath 出力ファイルパス
	 * @throws KadaiException
	 */
	public static void printMaxScore(String anInputPath, String anOutputPath) throws KadaiException {

		// 入出力ファイルnull・空文字チェック
		if (checkFile(anInputPath) || checkFile(anOutputPath)) {
			throw new KadaiException(KadaiConstants.FILE_INPUT_OUTPUT_ERROR);
		}

		// ファイルを読み込み、データの点数の積を取得
		Map<Integer, ScoreInfoBean> workTimeMap = readScoreFile(anInputPath);

		// データの点数の最高点を取得
		long maxScore = compareScore(workTimeMap);

		// データを書き込み
		writeScoreFile(anOutputPath, workTimeMap, maxScore);
	}

	/**
	 * ファイル読み込み
	 * <br>
	 * @param anInputPath 入力ファイルパス
	 * @return Map<Integer, ScoreInfoBean>
	 * @throws KadaiException
	 */
	private static Map<Integer, ScoreInfoBean> readScoreFile(String anInputPath) throws KadaiException {

		BufferedReader bufferedReader = null;

		Map<Integer, ScoreInfoBean> workTimeMap = new HashMap<Integer, ScoreInfoBean>();

		try {
			bufferedReader = new BufferedReader(new InputStreamReader(
					new FileInputStream(anInputPath), KadaiConstants.CHARACTER_CODE), KadaiConstants.FILE_SIZE);

			String oneRecord = null;

			// 1行目のみ処理するフラグ
			boolean calcFlag = false;

			// 入力ファイルを１行ずつ読み込む
			while (null != (oneRecord = bufferedReader.readLine())) {

				if (calcFlag) {
					// 2行目以降は処理をとばす
					break;
				}

				// BOM除去
				if (KadaiConstants.BOM_PATTERN == oneRecord.charAt(0)) {
					oneRecord = oneRecord.substring(1);
				}

				// レコードをカンマで区切る
				String[] dataScoreInfo = oneRecord.split(KadaiConstants.DELIMITER, -1);

				for (int i = 0 ; i < dataScoreInfo.length ; i++) {
					Matcher match = KadaiConstants.HALF_ALPHA_PATTERN.matcher(dataScoreInfo[i]);

					// 半角英字以外がデータに含まれている場合エラー
					if (!match.matches()) {
						throw new KadaiException(KadaiConstants.ILLEGAL_CHAR_ERROR);
					}

					// 小文字を大文字に変換
					String changeScoreInfo = dataScoreInfo[i].toUpperCase();

					// データの点数の積
					long scoreMultiply = 0;

					for (char textData : changeScoreInfo.toCharArray()) {

						// 文字の点数 * データの出現位置
						long score = (textData -'A' +1) * (i+1);
						scoreMultiply += score;
					}

					ScoreInfoBean bean = new ScoreInfoBean();
					bean.setTextData(dataScoreInfo[i]);
					bean.setScoreMultiply(scoreMultiply);

					workTimeMap.put(i+1, bean);

					// 1行目処理後にflagをtrueにする
					calcFlag = true;
				}
			}
		} catch (FileNotFoundException fne) {
			throw new KadaiException(KadaiConstants.FILE_INPUT_OUTPUT_ERROR);
		} catch (IOException e) {
			throw new KadaiException(KadaiConstants.OTHER_ERROR);
		} finally {
			if (null != bufferedReader) {
				try {
					bufferedReader.close();
				} catch (IOException e) {
					throw new KadaiException(KadaiConstants.OTHER_ERROR);
				}
			}
		}

		return workTimeMap;
	}

	/**
	 * 最大値取得
	 * <br>
	 * @param workTimeMap ファイル出力内容
	 * @return 最大値
	 */
	private static long compareScore(Map<Integer, ScoreInfoBean> workTimeMap) {

		long maxScore = 0;

		for (Entry<Integer, ScoreInfoBean> workTime : workTimeMap.entrySet()) {
			// 前の値と比較
			long biggerScore = Math.max(maxScore, workTime.getValue().getScoreMultiply());

			maxScore = biggerScore;
		}

		return maxScore;
	}

	/**
	 * ファイル書き込み
	 * <br>
	 * @param anOutputPath 出力ファイルパス
	 * @param workTimeMap ファイル出力内容
	 * @param aMaxScore 最大値
	 * @throws KadaiException
	 */
	private static void writeScoreFile(String anOutputPath, Map<Integer, ScoreInfoBean> workTimeMap,
			long aMaxScore) throws KadaiException {

		BufferedWriter bufferedWriter = null;

		try {
			bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(anOutputPath),
					KadaiConstants.CHARACTER_CODE));

			for (Entry<Integer, ScoreInfoBean> workTime : workTimeMap.entrySet()) {
				// 値が最大値の場合、ファイルに出力
				if (aMaxScore == workTime.getValue().getScoreMultiply()) {
					bufferedWriter.write(String.format(KadaiConstants.OUTPUT_FORMAT,
							String.valueOf(workTime.getKey()), workTime.getValue().getTextData(), String.valueOf(aMaxScore)));
					bufferedWriter.newLine();
					bufferedWriter.flush();
				}
			}
		} catch (FileNotFoundException fne) {
			throw new KadaiException(KadaiConstants.FILE_INPUT_OUTPUT_ERROR);
		} catch (IOException e) {
			throw new KadaiException(KadaiConstants.OTHER_ERROR);
		} finally {
			if (null != bufferedWriter) {
				try {
					bufferedWriter.close();
				} catch (IOException e) {
					throw new KadaiException(KadaiConstants.OTHER_ERROR);
				}
			}
		}
	}

	/**
	 * ファイル名チェック
	 * <br>
	 * @param anFilePath ファイル名
	 * @return boolean
	 * @throws KadaiException
	 */
	private static boolean checkFile(String aFilePath) {
		return null == aFilePath || aFilePath.isEmpty();
	}
}