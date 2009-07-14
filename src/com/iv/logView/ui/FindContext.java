package com.iv.logView.ui;

import com.iv.logView.model.FindModel;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FindContext {

    private final FindModel model;
    private final Finder finder;
    private final List<FindResult> findResults = new ArrayList<FindResult>();

    public FindContext(FindModel model) {
        this.model = model;
        if (model.isRegexp()) {
            finder = new RegExpFinder(model.getText(), model.isCaseSensitive());
        } else if (model.isCaseSensitive()) {
            finder = new CaseSensFinder(model.getText());
        } else {
            finder = new CaseInsensFinder(model.getText());
        }
    }

    public FindModel getModel() {
        return model;
    }

    public boolean accept(String str) {
        findResults.clear();
        return finder.accept(str);
    }

    public List<FindResult> getFindResults() {
        return findResults;
    }

    private abstract class Finder {
        public boolean accept(String str) {
            return accept(str, 0);
        }

        protected abstract boolean accept(String str, int fromIndex);
    }

    private class RegExpFinder extends Finder {
        private final Matcher matcher;

        public RegExpFinder(String str, boolean caseSens) {
            int flag = Pattern.DOTALL | (caseSens ? 0 : Pattern.CASE_INSENSITIVE);
            matcher = Pattern.compile(str, flag).matcher("");
        }

        public boolean accept(String str, int fromIndex) {
            matcher.reset(str);
            while (matcher.find(fromIndex)) {
                fromIndex = matcher.end();
                findResults.add(new FindResult(matcher.start(), matcher.end() - matcher.start()));
            }
            return findResults.size() > 0;
        }
    }

    private class CaseSensFinder extends Finder {
        protected final String findStr;

        public CaseSensFinder(String str) {
            this.findStr = str;
        }

        public boolean accept(String str, int fromIndex) {
            for (int n = str.indexOf(findStr, fromIndex); n >= 0; n = str.indexOf(findStr, fromIndex)) {
                findResults.add(new FindResult(n, findStr.length()));
                fromIndex = n + findStr.length();
                if (fromIndex < str.length()) {
                    accept(str, fromIndex);
                }
            }
            return findResults.size() > 0;
        }
    }

    private class CaseInsensFinder extends CaseSensFinder {

        public CaseInsensFinder(String str) {
            super(str.toLowerCase());
        }

        public boolean accept(String str) {
            return super.accept(str.toLowerCase());
        }

    }

}
