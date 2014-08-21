package com.redhat.victims;

/*
 * #%L
 * This file is part of victims-enforcer.
 * %%
 * Copyright (C) 2013 The Victims Project
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.artifact.filter.ScopeArtifactFilter;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilder;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilderException;
import org.apache.maven.shared.dependency.tree.traversal.CollectingDependencyNodeVisitor;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;



public interface ArtifactCollector {
    public ArtifactCollector with(EnforcerRuleHelper helper);
    public Set<Artifact> getArtifacts();
}


class BaseArtifactCollector implements ArtifactCollector {

    protected EnforcerRuleHelper helper;
    protected HashSet<Artifact> artifacts;
    protected MavenProject project;

    private void reset(){
        this.artifacts = new HashSet<Artifact>();
        try {
            project = (MavenProject) helper.evaluate("${project}");
        } catch (ExpressionEvaluationException e) {
        }
    }

    protected void gatherArtifacts() {

        for (Object a : project.getArtifacts()){

            if (a instanceof Artifact) {
                Artifact artifact = (Artifact) a;
                artifacts.add(artifact);
                helper.getLog().debug("[victims-enforcer] adding project dependency " + artifact.toString());
            }
        }
    }

    public ArtifactCollector with(EnforcerRuleHelper helper){
        this.helper = helper;
        return this;
    }

    public Set<Artifact> getArtifacts() {
        if (helper != null && (artifacts == null || artifacts.size() == 0)){
            reset();
            gatherArtifacts();
        }
        return artifacts;
    }
}


class DependencyTreeCollector extends BaseArtifactCollector {

    @Override
    protected void gatherArtifacts() {
        boolean e = false;
        try {

            ArtifactRepository localRepository = (ArtifactRepository) helper.evaluate("${localRepository}");
            DependencyTreeBuilder treeBuilder = (DependencyTreeBuilder) helper.getComponent(DependencyTreeBuilder.class);

            ScopeArtifactFilter filter = new ScopeArtifactFilter();
            filter.setIncludeRuntimeScopeWithImplications(true); // compile runtime

            DependencyNode treeRoot = treeBuilder.buildDependencyTree(project, localRepository, filter);
            helper.getLog().debug("[victims-enforcer] artifact id of root = "  + treeRoot.getArtifact().toString());

            CollectingDependencyNodeVisitor visitor = new CollectingDependencyNodeVisitor();
            treeRoot.accept(visitor);

            for (DependencyNode node : visitor.getNodes()){
                if (node.getState() == DependencyNode.INCLUDED && ! treeRoot.equals(node)){
                    Artifact artifact = node.getArtifact();
                    if (artifact != null){
                        artifacts.add(artifact);
                        helper.getLog().debug("[victims-enforcer] adding dependency " + artifact.toString());
                    }
                }
            }

        } catch (java.lang.NoSuchMethodError ex){
            helper.getLog().debug(ex); e = true;
        } catch (ComponentLookupException ex) {
            helper.getLog().debug(ex); e = true;
        } catch (ExpressionEvaluationException ex) {
            helper.getLog().debug(ex); e = true;
        } catch (DependencyTreeBuilderException ex) {
            helper.getLog().debug(ex); e = true;
        } finally {
            if (e){
                helper.getLog().info("[victims-enforcer] unable to find dependencies using: 'DependencyTreeBuilder'");
            }
        }
    }
}

class ReactorCollector extends BaseArtifactCollector{

    @Override
    protected void gatherArtifacts() {
        try {
            List<MavenProject> reactorProjects = (List<MavenProject>)helper.evaluate("${reactorProjects}");
            for (MavenProject rp: reactorProjects){
                for (Object item : rp.getArtifacts()){
                    if (item != null && item instanceof Artifact){
                        Artifact artifact = (Artifact) item;
                        artifacts.add(artifact);
                        helper.getLog().debug("[victims-enforcer] adding reactor dependency " + artifact.toString());
                    }
                }
            }
        } catch (ExpressionEvaluationException ex) {
            helper.getLog().debug(ex);
            helper.getLog().info("[victims-enforcer] unable to find dependencies using: 'ReactorCollector'");
        }
    }
}
